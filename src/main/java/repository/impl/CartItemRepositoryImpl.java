package repository.impl;

import entity.CartItem;
import repository.CartItemRepository;

public class CartItemRepositoryImpl extends BaseRepositoryImpl<CartItem> implements CartItemRepository {

    public CartItemRepositoryImpl() {
        super(CartItem.class);
    }

    /**
     * Returns the total quantity of a specific variant already in a user's cart,
     * summed across ALL line-items (i.e. all date ranges).
     * <p>
     * Used by CartService to enforce per-user inventory limits before adding.
     * <p>
     * JPQL path: CartItem → Cart → User (via Cart.user)
     */
    @Override
    public int sumQtyByVariantAndUser(Integer variantId, Integer userId) {
        Long result = em().createQuery(
                        "SELECT COALESCE(SUM(ci.quantity), 0) " +
                                "FROM CartItem ci " +
                                "WHERE ci.variant.id = :variantId " +
                                "AND ci.cart.user.id = :userId",
                        Long.class
                )
                .setParameter("variantId", variantId)
                .setParameter("userId", userId)
                .getSingleResult();

        return result.intValue();
    }
}