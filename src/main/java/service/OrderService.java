package service;

import com.google.gson.Gson;
import dto.AdminOrderDTO;
import dto.OrderDTO;
import entity.*;
import entity.enums.OrderStatus;
import exception.InsufficientFundsException;
import exception.UserNotFoundException;
import mapper.OrderMapper;
import org.mapstruct.factory.Mappers;
import repository.OrderRepository;
import repository.impl.OrderRepositoryImpl;
import util.JsonUtil;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderService extends BaseService<Order> {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final UserService userService;
    private final ProductVariantService productVariantService;
    private final OrderMapper mapper = Mappers.getMapper(OrderMapper.class);


    public OrderService() {
        this(new OrderRepositoryImpl());
    }

    public OrderService(OrderRepository orderRepository) {
        super(orderRepository);
        this.orderRepository = orderRepository;
        this.cartService = new CartService();
        this.userService = new UserService();
        this.productVariantService = new ProductVariantService();
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
                    return mapper.toOrderDTO(order);
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

    public Integer placeOrder(Integer userId, String cartJson, Double orderAmount) {

        // 1. Parse the cart JSON from localStorage
        //    cartJson is an array of normalised cart items (see CartItemNormaliser shape)
        List<Map> cartItems = List.of();

        try {
            cartItems = JsonUtil.fromJson(cartJson, List.class);
        } catch (IOException e) {
            System.out.println("Failed to convert cartJson back to List of cart items");
        }

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalStateException("Cart is empty.");
        }

        // 2. Load the user and their cart
        User user = userService.getById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found id [" + userId + "]"));

        if (user.getCreditLimit().intValue() < orderAmount) {
            throw new InsufficientFundsException(
                    "You don't have enough credit limit to make this order.");
        }

        user.setCreditLimit(user.getCreditLimit().subtract(new BigDecimal(orderAmount)));

        // 3. Create the Order entity
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.ORDERED);
        order.setTotalAmount(new BigDecimal(orderAmount));

        // 4. Create an OrderItem for each cart item
        for (Map item : cartItems) {
            int variantId = ((Double) item.get("id")).intValue(); // JS numbers come as Double in Gson
            int qty = ((Double) item.get("qty")).intValue();

            ProductVariant variant = productVariantService.getById(variantId)
                    .orElseThrow(() -> new IllegalStateException("Item no longer available."));

            // Optional: check stock is still sufficient
            if (variant.getQuantity() < qty) {
                throw new IllegalStateException(
                        variant.getProduct().getName() + " only has "
                                + variant.getProduct() + " left in stock."
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setVariant(variant);
            orderItem.setQuantity(qty);
            orderItem.setPriceAtPurchase(variant.getProduct().getBasePrice());
            orderItem.setStartDate(LocalDate.parse((String) item.get("startDate")));
            orderItem.setEndDate(LocalDate.parse((String) item.get("endDate")));
            order.addOrderItem(orderItem);
        }

        // 5. Save the order (cascades to order items)
        save(order);

        // 6. Clear the DB cart for this user
        Cart userCart = user.getCart();
        userCart.getCartItems().clear();
        cartService.save(userCart);

        return order.getId();
    }
}