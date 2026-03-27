package repository;

import entity.Category;
import entity.enums.Gender;

import java.util.List;

public interface CategoryRepository extends Repository<Category> {
    List<Category> findByGender(Gender gender);
}
