package service;

import model.User;
import repository.UserRepository;
import repository.impl.UserRepositoryImpl;
import util.HashUtil;

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

    public void register(User user) throws Exception {
        if (getUserByEmail(user.getEmail()).isPresent()) {
            throw new Exception("An account with this email already exists.");
        }
        user.setPasswordHash(HashUtil.hashPassword(user.getPasswordHash()));
        userRepository.save(user);
    }
}