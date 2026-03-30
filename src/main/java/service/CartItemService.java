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
}