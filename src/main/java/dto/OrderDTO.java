package dto;

import entity.enums.OrderStatus;

import java.math.BigDecimal;
import java.util.List;

public record OrderDTO(
        Integer id,
        OrderStatus status,
        BigDecimal totalAmount,
        String createdAt,
        List<OrderItemDTO> items
) {}
