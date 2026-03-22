package service;

import entity.Category;
import repository.CategoryRepository;
import repository.impl.CategoryRepositoryImpl;

public class CategoryService extends BaseService<Category> {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this(new CategoryRepositoryImpl());
    }

    public CategoryService(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }
}