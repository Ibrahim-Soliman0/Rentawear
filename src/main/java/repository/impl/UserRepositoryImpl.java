package repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import entity.User;
import repository.UserRepository;
import util.JPAUtil;

public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public User findByEmail(String email) {
        User user = null;
        try {
            user = em().createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Email not found!");
        }

        return user;
    }
}
