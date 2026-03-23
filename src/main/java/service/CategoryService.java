package service;

import entity.Category;
import entity.enums.Gender;
import repository.CategoryRepository;
import repository.impl.CategoryRepositoryImpl;

import java.util.List;

public class CategoryService extends BaseService<Category> {

    private final CategoryRepository categoryRepository;

    public CategoryService() {
        this(new CategoryRepositoryImpl());
    }

    public CategoryService(CategoryRepository categoryRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getByGender(Gender gender) {
        return categoryRepository.findByGender(gender);
    }
}