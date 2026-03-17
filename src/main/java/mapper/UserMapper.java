package mapper;

import dto.UserInterestDTO;
import dto.UserRegisterDTO;
import dto.UserSessionDTO;
import entity.User;
import entity.UserCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface UserMapper {

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

    @Mapping(target = "birthday", source = "birthday", dateFormat = "yyyy-MM-dd")
    UserSessionDTO toSessionDTO(User user);
}