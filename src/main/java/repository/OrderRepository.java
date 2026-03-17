package repository;

import entity.Order;

import java.util.List;

public interface OrderRepository extends Repository<Order> {

    /**
     * Returns all orders for a given user, oldest first.
     */
    List<Order> findByUserId(Integer userId);
}
