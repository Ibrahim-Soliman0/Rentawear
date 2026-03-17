package mapper;

import dto.OrderDTO;
import dto.OrderItemDTO;
import entity.Order;
import entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {

    @Mapping(target = "customerName",  source = "user.name")
    @Mapping(target = "customerEmail", source = "user.email")
    OrderDTO toOrderDTO(Order order);

    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "color",       source = "variant.color")
    @Mapping(target = "size",        source = "variant.size")
    OrderItemDTO toOrderItemDTO(OrderItem orderItem);
}