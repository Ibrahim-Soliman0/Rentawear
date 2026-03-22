package repository.impl;

import entity.Order;
import repository.OrderRepository;

import java.util.List;

public class OrderRepositoryImpl extends BaseRepositoryImpl<Order> implements OrderRepository {

    public OrderRepositoryImpl() {
        super(Order.class);
    }

    @Override
    public List<Order> findByUserId(Integer userId) {
        /* Step 1: load orders with their items and variants in one query.
           We do NOT fetch productImages here to avoid a cartesian product
           with orderItems — we fetch them separately per order in Step 2. */
        List<Order> orders = em().createQuery(
                        "SELECT DISTINCT o FROM Order o " +
                                "LEFT JOIN FETCH o.orderItems oi " +
                                "LEFT JOIN FETCH oi.variant v " +
                                "LEFT JOIN FETCH v.product p " +
                                "WHERE o.user.id = :userId " +
                                "ORDER BY o.createdAt DESC",
                        Order.class)
                .setParameter("userId", userId)
                .getResultList();

        /* Step 2: force-load productImages for each product while the
           EntityManager is still open (avoids LazyInitializationException
           in the mapper and avoids the cartesian explosion in Step 1). */
        for (Order order : orders) {
            for (var item : order.getOrderItems()) {
                item.getVariant().getProduct().getProductImages().size();
            }
        }

        return orders;
    }
}

