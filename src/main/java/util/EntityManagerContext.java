package util;

import jakarta.persistence.EntityManager;

public class EntityManagerContext {

    private static final ThreadLocal<EntityManager> holder = new ThreadLocal<>();

    public static void set(EntityManager em) {
        holder.set(em);
    }

    public static EntityManager get() {
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }
}