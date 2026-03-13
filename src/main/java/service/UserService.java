package service;

import exception.EmailAlreadyExistsException;
import model.User;
import dto.UserProfileDTO;
import entity.User;
import mapper.UserMapper;
import org.mapstruct.factory.Mappers;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;
import util.HashUtil;

import java.util.Optional;

public class UserService extends BaseService<User> {

    private final UserRepository userRepository;

    private final UserMapper mapper = Mappers.getMapper(UserMapper.class);

    public UserService() {
        this(new UserRepositoryImpl());
    }

    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    public void register(User user) throws EmailAlreadyExistsException {
        if (getUserByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        user.setPasswordHash(HashUtil.hashPassword(user.getPasswordHash()));
        userRepository.save(user);
    }

    public Optional<UserProfileDTO> getProfileDetails(Integer id) {

        User user = userRepository.findById(id);

        return Optional.ofNullable(mapper.toProfileDto(user));
    }
}