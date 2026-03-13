package repository;

import java.util.List;

public interface Repository<T> {

    T findById(Integer id);

    T save(T entity);

    void delete(T entity);

    List<T> findAll();
}
