package repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import model.User;
import repository.UserRepository;
import util.JPAUtil;

import java.util.Optional;

public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public User findByEmail(String email) {
        User user = null;
        try (EntityManager em = JPAUtil.getEntityManager()) {
            user = em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            System.out.println("Email not found!");
        }

        return user;
    }
}
