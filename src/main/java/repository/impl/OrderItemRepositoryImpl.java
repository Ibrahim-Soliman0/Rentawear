package repository.impl;

import entity.OrderItem;
import repository.OrderItemRepository;

public class OrderItemRepositoryImpl extends BaseRepositoryImpl<OrderItem> implements OrderItemRepository {

    public OrderItemRepositoryImpl() {
        super(OrderItem.class);
    }
}

