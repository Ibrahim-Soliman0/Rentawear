package service;

import model.Order;
import repository.OrderRepository;
import repository.impl.OrderRepositoryImpl;

public class OrderService extends BaseService<Order> {

    private final OrderRepository orderRepository;

    public OrderService() {
        this(new OrderRepositoryImpl());
    }

    public OrderService(OrderRepository orderRepository) {
        super(orderRepository);
        this.orderRepository = orderRepository;
    }
}