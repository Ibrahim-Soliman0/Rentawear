package repository.impl;

import jakarta.persistence.EntityManager;
import model.User;
import repository.UserRepository;
import util.JPAUtil;

public class UserRepositoryImpl extends BaseRepositoryImpl<User> implements UserRepository {

    public UserRepositoryImpl() {
        super(User.class);
    }

    @Override
    public User findByEmail(String email) {
        try (EntityManager em = JPAUtil.getEntityManager()) {

            return em.createNamedQuery("User.findByEmail", User.class)
                    .setParameter("email", email)
                    .getSingleResult();
        }
    }
}
