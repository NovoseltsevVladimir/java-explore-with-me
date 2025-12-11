package ru.practicum.ewm.category.mapper;

import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.model.Category;

public class CategoryMapper {

    public static CategoryDto mapToCategoryDto(Category category) {

        CategoryDto dto = new CategoryDto();
        dto.setName(category.getName());
        dto.setId(category.getId());

        return dto;
    }

    public static Category mapToCategory(NewCategoryDto newCategoryDto) {

        Category category = new Category();
        category.setName(newCategoryDto.getName());

        return category;
    }
}

