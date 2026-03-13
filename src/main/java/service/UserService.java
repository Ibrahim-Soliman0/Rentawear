package service;

import entity.User;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;

import java.util.Optional;

public class UserService extends BaseService<User> {

    private final UserRepository userRepository;

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
}