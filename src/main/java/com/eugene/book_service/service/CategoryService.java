package com.eugene.book_service.service;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.exception.DuplicatedException;
import com.eugene.book_service.exception.NotFoundException;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private static String getCategoryNotFoundMessage(long idCategory) {
        return "Category '" + idCategory + "' not found.";
    }

    @Transactional
    public Category createCategory(CategoryDto categoryDto) {
        boolean exists = categoryRepository
                .findByName(categoryDto.name())
                .isPresent();
        if (exists) {
            throw new DuplicatedException(
                    "Category '" + categoryDto.name() + "' " + "already exists.", null);
        } else {
            Category category = new Category(categoryDto.name());
            return categoryRepository.save(category);
        }
    }

    @Transactional
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Transactional
    public Category getCategoryById(Long idCategory) {

        return categoryRepository
                .findById(idCategory)
                .orElseThrow(
                        () -> new NotFoundException(getCategoryNotFoundMessage(idCategory), null));
    }

    @Transactional
    public Category updateCategory(Long idCategory, CategoryDto categoryDto) {
        Category categoryOld = categoryRepository
                .findById(idCategory)
                .orElseThrow(
                        () -> new NotFoundException(getCategoryNotFoundMessage(idCategory), null));
        categoryOld.setName(categoryDto.name());
        return categoryRepository.save(categoryOld);
    }

    @Transactional
    public void deleteCategory(Long idCategory) {
        categoryRepository.deleteById(idCategory);
    }
}
