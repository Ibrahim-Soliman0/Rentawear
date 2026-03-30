package service;

import entity.CartItem;
import repository.CartItemRepository;
import repository.impl.CartItemRepositoryImpl;

public class CartItemService extends BaseService<CartItem> {

    private final CartItemRepository cartItemRepository;

    public CartItemService() {
        this(new CartItemRepositoryImpl());
    }

    public CartItemService(CartItemRepository cartItemRepository) {
        super(cartItemRepository);
        this.cartItemRepository = cartItemRepository;
    }

    /**
     * Returns the total quantity of a specific variant already sitting in the
     * user's DB cart, summed across all date ranges.
     * <p>
     * Called by CartVariantQtyServlet so cart.js can factor in DB-reserved stock
     * when performing the client-side inventory check for guest users (who have
     * an empty localStorage after logout but may still have items in their DB cart).
     */
    public int getReservedQty(Integer userId, Integer variantId) {
        return cartItemRepository.sumQtyByVariantAndUser(variantId, userId);
    }


}