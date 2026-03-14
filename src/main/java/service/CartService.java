package service;

import entity.Cart;
import repository.CartRepository;
import repository.impl.CartRepositoryImpl;

public class CartService extends BaseService<Cart> {

    private final CartRepository cartRepository;

    public CartService() {
        this(new CartRepositoryImpl());
    }

    public CartService(CartRepository cartRepository) {
        super(cartRepository);
        this.cartRepository = cartRepository;
    }
}