package mapper;

import dto.*;
import entity.User;
import entity.UserCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.control.MappingControl;

@Mapper(uses = PaymentCardMapper.class)
public interface UserMapper {

    UserProfileDTO toProfileDto(User user);

    User toEntity(UserProfileDTO dto);

    @Mapping(target = "gender",       expression = "java(entity.enums.Gender.valueOf(dto.gender().toUpperCase()))")
    @Mapping(target = "interests",    ignore = true)
    @Mapping(target = "paymentCards", ignore = true)
    @Mapping(target = "cart",         ignore = true)
    @Mapping(target = "role",         ignore = true)
    @Mapping(target = "createdAt",    ignore = true)
    User toEntity(UserRegisterDTO dto);

    @Mapping(target = "categoryId",   source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    UserInterestDTO toInterestDTO(UserCategory userCategory);

    UserSessionDTO toSessionDTO(User user);

    CustomerDTO toCustomerDto(User user);

}