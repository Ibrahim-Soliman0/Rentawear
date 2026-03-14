package mapper;

import dto.UserProfileDTO;
import entity.User;
import org.mapstruct.Mapper;

@Mapper(uses = PaymentCardMapper.class)
public interface UserMapper {

    UserProfileDTO toProfileDto(User user);

    User toEntity(UserProfileDTO dto);
}