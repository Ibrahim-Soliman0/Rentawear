package service;

import entity.OrderItem;
import repository.OrderItemRepository;
import repository.impl.OrderItemRepositoryImpl;

public class OrderItemService extends BaseService<OrderItem> {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService() {
        this(new OrderItemRepositoryImpl());
    }

    public OrderItemService(OrderItemRepository orderItemRepository) {
        super(orderItemRepository);
        this.orderItemRepository = orderItemRepository;
    }
}