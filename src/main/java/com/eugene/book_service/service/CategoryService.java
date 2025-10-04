package com.eugene.book_service.service;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.exception.DuplicatedException;
import com.eugene.book_service.exception.NotFoundException;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.CategoryRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService
{
    private final CategoryRepository categoryRepository;
    
    private static String getCategoryNotFoundMessage(long idCategory) {
        return "Category '" + idCategory + "' not found.";
    }
    
    @Transactional
    public Category createCategory(@Valid CategoryDto categoryDto) {
        if (this.categoryRepository
                .findByName(categoryDto.getName())
                .isPresent()) {
            throw new DuplicatedException(
                    "Category '" + categoryDto.getName() + "' " + "already exists.",
                    null);
        } else {
            Category category = new Category(categoryDto.getName());
            return this.categoryRepository.save(category);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return this.categoryRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Category getCategoryById(Long idCategory) {
        
        return this.categoryRepository
                .findById(idCategory)
                .orElseThrow(() -> new NotFoundException(getCategoryNotFoundMessage(idCategory),
                                                         null));
    }
    
    @Transactional
    public Category updateCategory(
            Long idCategory,
            @Valid CategoryDto categoryDto
    ) {
        Category category = this.categoryRepository
                .findById(idCategory)
                .orElseThrow(() -> new NotFoundException(getCategoryNotFoundMessage(idCategory),
                                                         null));
        
        this.categoryRepository
                .findByName(categoryDto.getName())
                .ifPresent(existing -> {
                    throw new DuplicatedException(
                            "Category '" + categoryDto.getName() + "' already exists.",
                            null);
                });
        
        category.setName(categoryDto.getName());
        return this.categoryRepository.save(category);
    }
    
    @Transactional
    public void deleteCategory(Long idCategory) {
        Category category = this.categoryRepository
                .findById(idCategory)
                .orElseThrow(() -> new NotFoundException(getCategoryNotFoundMessage(idCategory),
                                                         null));
        this.categoryRepository.delete(category);
    }
}
