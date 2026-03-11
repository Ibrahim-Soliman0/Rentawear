package repository;

import model.User;

public interface UserRepository extends Repository<User> {

    User findByEmail(String email);
}
