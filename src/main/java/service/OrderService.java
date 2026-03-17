package service;

import dto.AdminOrderDTO;
import entity.Order;
import mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import repository.OrderRepository;
import repository.impl.OrderRepositoryImpl;

import java.util.List;
import java.util.stream.Collectors;

public class OrderService extends BaseService<Order> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    public OrderService() {
        this(new OrderRepositoryImpl());
    }

    public OrderService(OrderRepository orderRepository) {
        super(orderRepository);
        this.orderRepository = orderRepository;
    }

    public List<AdminOrderDTO> getAllOrders() {
        return orderRepository.findAll()
                .stream()
                .map(order -> {
                    // Force load lazy associations
                    order.getUser();
                    order.getOrderItems().forEach(item -> {
                        item.getVariant().getProduct().getName();
                    });
                    return orderMapper.toOrderDTO(order);
                })
                .collect(Collectors.toList());
    }
}