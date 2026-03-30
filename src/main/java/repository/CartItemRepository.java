package repository;

import entity.CartItem;

import java.util.List;

public interface CartItemRepository extends Repository<CartItem> {
    void deleteByVariantIds(List<Integer> variantIds);

}
