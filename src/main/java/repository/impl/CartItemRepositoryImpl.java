package repository.impl;

import entity.CartItem;
import repository.CartItemRepository;

import java.util.List;

public class CartItemRepositoryImpl extends BaseRepositoryImpl<CartItem>
        implements CartItemRepository {

    public CartItemRepositoryImpl() {
        super(CartItem.class);
    }

    @Override
    public void deleteByVariantIds(List<Integer> variantIds) {
        if (variantIds == null || variantIds.isEmpty()) return;
        em().createQuery(
                        "DELETE FROM CartItem ci WHERE ci.variant.id IN :vids")
                .setParameter("vids", variantIds)
                .executeUpdate();
    }
}