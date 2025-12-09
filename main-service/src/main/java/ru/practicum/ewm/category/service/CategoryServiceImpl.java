package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.exception.BadRequestException;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private String logInfo;

    //Admin
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {

        String categoryName = newCategoryDto.getName();

        if (categoryRepository.existsByName(categoryName)) {
            logInfo = "Категория с именем '" + categoryName + "' уже существует.";
            log.error(logInfo);
            throw new ValidationException(logInfo);
        }

        Category category = CategoryMapper.mapToCategory(newCategoryDto);
        Category savedCategory = categoryRepository.save(category);

        return CategoryMapper.mapToCategoryDto(savedCategory);
    }

    @Override
    public CategoryDto updateCategory(Long catId, CategoryDto categoryDto) {

        Category categoryToUpdate = getCategoryByIdWithException(catId);

        //Новое имя не должно использоваться в других категориях
        String newCategoryName = categoryDto.getName();

        if (newCategoryName.equals(categoryToUpdate.getName())) {
            return CategoryMapper.mapToCategoryDto(categoryToUpdate);
        }

        if (newCategoryName.length() > 50 || newCategoryName.length() < 1) {
            logInfo = "Некорректное имя категории " + newCategoryName;
            log.error(logInfo);
            throw new BadRequestException(logInfo);
        }

        if (categoryRepository.existsByName(newCategoryName)) {
            logInfo = "Категория с именем " + newCategoryName + " уже существует.";
            log.error(logInfo);
            throw new ValidationException(logInfo);
        }

        categoryToUpdate.setName(newCategoryName);
        Category updatedCategory = categoryRepository.save(categoryToUpdate);

        return CategoryMapper.mapToCategoryDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long catId) {

        if (!categoryRepository.existsById(catId)) {
            logInfo = "Категория с id " + catId + " не найдена.";
            log.error(logInfo);
            throw new NotFoundException(logInfo);
        }

        categoryRepository.deleteById(catId);
    }

    //Public
    @Override
    public CategoryDto getCategoryById(Long catId) {
        return CategoryMapper.mapToCategoryDto(getCategoryByIdWithException(catId));
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        PageRequest pageable = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageable).getContent();

        return categories.stream()
                .map(CategoryMapper::mapToCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public Category getCategoryByIdWithException(Long categoryId) {
        Optional<Category> categoryOptional = categoryRepository.findById(categoryId);
        if (categoryOptional.isEmpty()) {
            logInfo = "Категория с id " + categoryId + " не найдена.";
            log.error(logInfo);
            throw new NotFoundException(logInfo);
        }

        return categoryOptional.get();
    }
}

