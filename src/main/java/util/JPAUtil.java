package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JPAUtil {

    private static volatile EntityManagerFactory emf;

    private JPAUtil() {

    }

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (JPAUtil.class) {
                if (emf == null) {
                    try {
                        emf = Persistence.createEntityManagerFactory("rentawear_db");
                    } catch (Exception e) {
                        System.err.println("Initial EntityManagerFactory creation failed: " + e);
                        throw new ExceptionInInitializerError(e);
                    }
                }
            }
        }

        return emf;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    public static void close() {
        if (emf.isOpen()) {
            emf.close();
        }
    }
}