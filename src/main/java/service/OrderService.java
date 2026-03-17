package service;

import dto.OrderDTO;
import dto.OrderItemDTO;
import entity.Order;
import entity.OrderItem;
import entity.enums.OrderStatus;
import mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import repository.OrderRepository;
import repository.impl.OrderRepositoryImpl;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class OrderService extends BaseService<Order> {

    private final OrderRepository orderRepository;

    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);


    public OrderService() {
        this(new OrderRepositoryImpl());
    }

    public OrderService(OrderRepository orderRepository) {
        super(orderRepository);
        this.orderRepository = orderRepository;
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