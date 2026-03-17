package service;

import dto.AdminOrderDTO;
import dto.OrderDTO;
import dto.OrderItemDTO;
import entity.Order;
import mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import entity.OrderItem;
import entity.enums.OrderStatus;
import mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import repository.OrderRepository;
import repository.impl.OrderRepositoryImpl;

import java.util.List;
import java.util.stream.Collectors;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class OrderService extends BaseService<Order> {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper = Mappers.getMapper(OrderMapper.class);

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);


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

    /**
     * Returns two lists: [0] = active orders (ORDERED → DELIVERED),
     * [1] = past orders   (RETURNED + CANCELLED)
     * Both lists are ordered oldest → newest (from DB).
     */
    public List<List<OrderDTO>> getOrdersForUser(Integer userId) {
        List<Order> all = orderRepository.findByUserId(userId);

        List<OrderDTO> active = new ArrayList<>();
        List<OrderDTO> past = new ArrayList<>();

        for (Order order : all) {
            OrderDTO dto = mapper.toDTO(order);
            if (order.getStatus() == OrderStatus.RETURNED || order.getStatus() == OrderStatus.CANCELLED) {
                past.add(dto);
            } else {
                active.add(dto);
            }
        }

        return List.of(active, past);
    }
}