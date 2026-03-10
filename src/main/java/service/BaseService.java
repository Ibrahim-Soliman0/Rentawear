package service;

import repository.Repository;

public abstract class BaseService<T> {

    protected final Repository<T> repository;

    protected BaseService(Repository<T> repository) {
        this.repository = repository;
    }

    public T getById(Integer id) {
        return repository.findById(id);
    }

    public void save(T entity) {
        repository.save(entity);
    }

    public void delete(T entity) {
        repository.delete(entity);
    }
}