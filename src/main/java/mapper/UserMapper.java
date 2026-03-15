package mapper;

import dto.UserRegisterDTO;
import dto.UserProfileDTO;
import entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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
}