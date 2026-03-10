package service;

import model.Cart;
import model.User;
import repository.CartRepository;
import repository.UserRepository;
import repository.impl.CartRepositoryImpl;
import repository.impl.UserRepositoryImpl;

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