package repository.impl;

import entity.Order;
import repository.OrderRepository;

public class OrderRepositoryImpl extends BaseRepositoryImpl<Order> implements OrderRepository {

    public OrderRepositoryImpl() {
        super(Order.class);
    }
}

