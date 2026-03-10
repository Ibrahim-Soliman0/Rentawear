package service;

import model.User;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;

public class UserService extends BaseService<User> {

    private final UserRepository userRepository;

    public UserService() {
        this(new UserRepositoryImpl());
    }

    public UserService(UserRepository userRepository) {
        super(userRepository);
        this.userRepository = userRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}