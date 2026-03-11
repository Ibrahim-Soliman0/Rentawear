package repository.impl;

import model.CartItem;
import repository.CartItemRepository;

public class CartItemRepositoryImpl extends BaseRepositoryImpl<CartItem> implements CartItemRepository {

    public CartItemRepositoryImpl() {
        super(CartItem.class);
    }
}

