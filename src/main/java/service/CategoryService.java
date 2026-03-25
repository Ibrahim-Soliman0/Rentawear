package service;

import dto.CategoryDTO;
import dto.SaveCategoryDTO;
import entity.Category;
import entity.enums.Gender;
import mapper.CategoryMapper;
import org.mapstruct.factory.Mappers;
import repository.CategoryRepository;
import repository.impl.CategoryRepositoryImpl;

import java.util.List;

public class CategoryService extends BaseService<Category> {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper = Mappers.getMapper(CategoryMapper.class);

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

    public CategoryDTO saveCategory(SaveCategoryDTO dto) {
        Category category = categoryMapper.toEntity(dto);
        Category saved    = repository.save(category);
        return categoryMapper.toDTO(saved);
    }
}