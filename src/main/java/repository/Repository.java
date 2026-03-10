package repository;

public interface Repository<T> {

    T findById(Integer id);

    void save(T entity);

    void delete(T entity);
}
