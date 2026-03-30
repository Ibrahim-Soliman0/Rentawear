package repository;

import entity.CartItem;

public interface CartItemRepository extends Repository<CartItem> {

    /**
     * Sum of quantity for a given variantId across all of a user's cart line-items.
     * Returns 0 if the user has no items for that variant.
     */
    int sumQtyByVariantAndUser(Integer variantId, Integer userId);
}
