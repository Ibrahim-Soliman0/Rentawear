package repository.impl;

import model.Cart;
import repository.CartRepository;

public class CartRepositoryImpl extends BaseRepositoryImpl<Cart> implements CartRepository {

    public CartRepositoryImpl() {
        super(Cart.class);
    }
}

