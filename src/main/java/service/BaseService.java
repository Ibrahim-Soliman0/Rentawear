package service;

import repository.Repository;

import java.util.List;

import java.util.Optional;

public abstract class BaseService<T> {

    protected final Repository<T> repository;

    protected BaseService(Repository<T> repository) {
        this.repository = repository;
    }

    public Optional<T> getById(Integer id) {
        return Optional.ofNullable(repository.findById(id));
    }

    public T save(T entity) {
        return repository.save(entity);
    }

    public void delete(T entity) {
        repository.delete(entity);
    }

    public List<T> getAll(){ return repository.findAll();}
}