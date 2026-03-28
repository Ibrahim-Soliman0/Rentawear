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
import repository.CartItemRepository;
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
        super(cartRepository);
        this.cartRepository = cartRepository;
        this.userService = new UserService();
        this.productVariantService = new ProductVariantService();
        this.cartItemService = new CartItemService();
    }

    public List<CartItemDTO> getItems(Integer userId) {
        CartItemMapper mapper = Mappers.getMapper(CartItemMapper.class);
        User user = userService.getById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException("User with id [" + userId + "] doesn't exit"));

        List<CartItem> itemsInCart = user.getCart().getCartItems();
        return mapper.toDTOList(itemsInCart);
    }

    public Integer addItem(Integer userId, SimpleCartItemDTO itemDTO) {

        Optional<User> userOptional = userService.getById(userId);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with id [" + userId + "] doesn't exit");
        }

        User user = userOptional.get();
        Cart userCart = user.getCart();

        // first time renting the user has no cart created for him yet so create one
        if (userCart == null) {
            Cart cart = new Cart();
            user.setCart(cart);
        }

        userCart = user.getCart();

        ProductVariant productVariant = productVariantService.getById(itemDTO.variantId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Variant with id [" + itemDTO.variantId() + "] doesn't exist"));

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

        Optional<User> userOptional = userService.getById(userId);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with id [" + userId + "] doesn't exit");
        }

        User user = userOptional.get();
        Cart userCart = user.getCart();

        if (userCart == null) {
            System.out.println("the user id [" + userId + "] has no purchases made yet");
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        CartItem itemToRemove = cartItemService.getById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Cart item with id [" + cartItemId + "] doesn't exist"));

        if (userCart.getCartItems() == null || !userCart.getCartItems().contains(itemToRemove)) {
            System.out.println("the user id [" + userId + "] has no items in the cart or" +
                    " doesn't have the requested item");
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        userCart.removeCartItem(itemToRemove);
    }

    public void updateItemQty(Integer userId, Integer cartItemId, Integer newQty) {

        Optional<User> userOptional = userService.getById(userId);

        if (userOptional.isEmpty()) {
            throw new UserNotFoundException("User with id [" + userId + "] doesn't exit");
        }

        User user = userOptional.get();
        Cart userCart = user.getCart();

        if (userCart == null) {
            System.out.println("the user id [" + userId + "] has no purchases made yet");
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        CartItem itemToUpdate = cartItemService.getById(cartItemId)
                .orElseThrow(() -> new CartItemNotFoundException(
                        "Cart item with id [" + cartItemId + "] doesn't exist"));

        if (userCart.getCartItems() == null || !userCart.getCartItems().contains(itemToUpdate)) {
            System.out.println("the user id [" + userId + "] has no items in the cart or" +
                    " doesn't have the requested item");
            throw new CartItemNotFoundException(
                    "Cart item with id [" + cartItemId + "] doesn't exist");
        }

        itemToUpdate.setQuantity(newQty);
    }
}