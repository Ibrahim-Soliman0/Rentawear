package mapper;

import dto.AdminOrderDTO;
import dto.AdminOrderItemDTO;
import entity.Order;
import entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface OrderMapper {

    @Mapping(target = "customerName",  source = "user.name")
    @Mapping(target = "customerEmail", source = "user.email")
    AdminOrderDTO toOrderDTO(Order order);

    @Mapping(target = "productName", source = "variant.product.name")
    @Mapping(target = "color",       source = "variant.color")
    @Mapping(target = "size",        source = "variant.size")
    AdminOrderItemDTO toOrderItemDTO(OrderItem orderItem);
}