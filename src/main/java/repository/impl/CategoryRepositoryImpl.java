package repository.impl;

import entity.Category;
import repository.CategoryRepository;

public class CategoryRepositoryImpl extends BaseRepositoryImpl<Category> implements CategoryRepository {

    public CategoryRepositoryImpl() {
        super(Category.class);
    }
}

