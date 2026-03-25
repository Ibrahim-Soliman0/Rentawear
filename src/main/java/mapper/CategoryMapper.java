package mapper;

import dto.CategoryDTO;
import dto.SaveCategoryDTO;
import entity.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface CategoryMapper {

    @Mapping(target = "id",       ignore = true)
    @Mapping(target = "users",    ignore = true)
    @Mapping(target = "products", ignore = true)
    @Mapping(target = "gender",   expression = "java(entity.enums.Gender.valueOf(dto.gender().toUpperCase()))")
    Category toEntity(SaveCategoryDTO dto);

    CategoryDTO toDTO(Category category);
}