package repository.impl;

import entity.Category;
import entity.enums.Gender;
import repository.CategoryRepository;

import java.util.List;

public class CategoryRepositoryImpl extends BaseRepositoryImpl<Category> implements CategoryRepository {

    public CategoryRepositoryImpl() {
        super(Category.class);
    }

    @Override
    public List<Category> findByGender(Gender gender) {
        return em().createQuery(
                        "SELECT c FROM Category c WHERE c.gender = :gender ORDER BY c.name ASC",
                        Category.class)
                .setParameter("gender", gender)
                .getResultList();
    }
}

