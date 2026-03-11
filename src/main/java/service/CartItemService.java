package service;

import model.CartItem;
import repository.CartItemRepository;
import repository.impl.CartItemRepositoryImpl;

public class CartItemService extends BaseService<CartItem> {

    private final CartItemRepository cartItemRepository;

    public CartItemService() {
        this(new CartItemRepositoryImpl());
    }

    public CartItemService(CartItemRepository cartItemRepository) {
        super(cartItemRepository);
        this.cartItemRepository = cartItemRepository;
    }
}