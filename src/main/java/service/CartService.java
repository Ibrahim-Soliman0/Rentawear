package service;

import dto.CartItemDTO;
import dto.SimpleCartItemDTO;
import entity.Cart;
import entity.CartItem;
import entity.ProductVariant;
import entity.User;
import exception.CartItemNotFoundException;
import exception.UserNotFoundException;
import mapper.CartItemMapper;
import org.mapstruct.factory.Mappers;
import repository.CartRepository;
import repository.impl.CartItemRepositoryImpl;
import repository.impl.CartRepositoryImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class CartService extends BaseService<Cart> {

    private final CartRepository cartRepository;
    private final CartItemService cartItemService;
    private final UserService userService;
    private final ProductVariantService productVariantService;

    public CartService() {
        this(new CartRepositoryImpl());
    }

    public CartService(CartRepository cartRepository) {
        this(
                cartRepository,
                new UserService(),
                new ProductVariantService(),
                new CartItemService()
        );
    }

    public CartService(
            CartRepository cartRepository,
            UserService userService,
            ProductVariantService productVariantService,
            CartItemService cartItemService
    ) {
        super(cartRepository);
        this.cartRepository = cartRepository;
        this.userService = userService;
        this.productVariantService = productVariantService;
        this.cartItemService = cartItemService;
    }

    public List<CartItemDTO> getItems(Integer userId) {
        CartItemMapper mapper = Mappers.getMapper(CartItemMapper.class);
        User user = userService.getById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id [" + userId + "] doesn't exist"));

        List<CartItem> itemsInCart = user.getCart().getCartItems();
        return mapper.toDTOList(itemsInCart);
    }

    public Integer addItem(Integer userId, SimpleCartItemDTO itemDTO) {

        User user = userService.getById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id [" + userId + "] doesn't exist"));

        Cart userCart = user.getCart();

        // first time renting the user has no cart created for him yet so create one
        if (userCart == null) {
            Cart cart = new Cart();
            user.setCart(cart);
            userCart = user.getCart();
        }

        ProductVariant productVariant = productVariantService.getById(itemDTO.variantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant with id [" + itemDTO.variantId() + "] doesn't exist"));

        // ── Server-side inventory enforcement ─────────────────────────────────
        // Count how many of this variant the user already has across ALL their
        // cart line-items (all date ranges).  Reject if adding the requested qty
        // would exceed the variant's inventory.
        //
        // This is the authoritative check — the client-side check in cart.js is
        // a UX convenience only.  A logged-out user who had 3 items in their DB
        // cart cannot bypass the limit by adding more as a guest and then merging.
        int alreadyInCart = cartItemService.getReservedQty(userId, itemDTO.variantId());
        int inventoryQty = productVariant.getQuantity();

        if (alreadyInCart + itemDTO.qty() > inventoryQty) {
            int remaining = inventoryQty - alreadyInCart;
            throw new IllegalStateException(
                    remaining <= 0
                            ? "No stock remaining for this variant."
                            : "Only " + remaining + " unit(s) of this variant available."
            );
        }

        CartItem itemToAdd = new CartItem();
        itemToAdd.setVariant(productVariant);
        itemToAdd.setQuantity(itemDTO.qty());
        itemToAdd.setStartDate(LocalDate.parse(itemDTO.startDate()));
        itemToAdd.setEndDate(LocalDate.parse(itemDTO.endDate()));

        itemToAdd = cartItemService.save(itemToAdd);

        userCart.addCartItem(itemToAdd);

        return itemToAdd.getId();
    }

    public void removeItem(Integer userId, Integer cartItemId) {

        User user = userService.getById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id [" + userId + "] doesn't exist"));

        Cart userCart = user.getCart();

        if (userCart == null) {
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        CartItem itemToRemove = cartItemService.getById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Cart item with id [" + cartItemId + "] doesn't exist"));

        if (userCart.getCartItems() == null || !userCart.getCartItems().contains(itemToRemove)) {
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        userCart.removeCartItem(itemToRemove);
    }

    public void updateItemQty(Integer userId, Integer cartItemId, Integer newQty) {

        User user = userService.getById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id [" + userId + "] doesn't exist"));

        Cart userCart = user.getCart();

        if (userCart == null) {
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        CartItem itemToUpdate = cartItemService.getById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Cart item with id [" + cartItemId + "] doesn't exist"));

        if (userCart.getCartItems() == null || !userCart.getCartItems().contains(itemToUpdate)) {
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        // ── Server-side inventory enforcement for qty updates ─────────────────
        // Subtract the item's current qty before checking, because we're
        // replacing it — not adding on top of it.
        int currentQty = itemToUpdate.getQuantity();
        int otherQty = cartItemService.getReservedQty(userId,
                itemToUpdate.getVariant().getId()) - currentQty;
        int inventoryQty = itemToUpdate.getVariant().getQuantity();

        if (otherQty + newQty > inventoryQty) {
            int remaining = inventoryQty - otherQty;
            throw new IllegalStateException(
                    remaining <= 0
                            ? "No stock remaining for this variant."
                            : "Only " + remaining + " unit(s) of this variant available."
            );
        }

        itemToUpdate.setQuantity(newQty);
    }
}