package service;

import entity.CartItem;
import repository.CartItemRepository;
import repository.impl.CartItemRepositoryImpl;

import java.util.List;

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
     * Hard-deletes all cart items whose variant_id is in the supplied list.
     * Must be called before soft-deleting any variant so that cart_items no
     * longer holds a live FK pointing at the about-to-be-hidden row.
     */
    public void deleteByVariantIds(List<Integer> variantIds) {
        cartItemRepository.deleteByVariantIds(variantIds);
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