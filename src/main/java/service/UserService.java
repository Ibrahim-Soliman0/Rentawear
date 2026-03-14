package service;

import exception.EmailAlreadyExistsException;
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

    public void register(User user) throws EmailAlreadyExistsException {
        if (getUserByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException(user.getEmail());
        }
        user.setPasswordHash(HashUtil.hashPassword(user.getPasswordHash()));
        userRepository.save(user);
    }

    public Optional<User> login(String email, String password) {
        Optional<User> userOpt = getUserByEmail(email);

        // Check user exists and password matches
        if (userOpt.isEmpty()) return Optional.empty();

        User user = userOpt.get();
        if (!HashUtil.verifyPassword(password,user.getPasswordHash())) {
            return Optional.empty();
        }
        return Optional.of(user);
    }
}