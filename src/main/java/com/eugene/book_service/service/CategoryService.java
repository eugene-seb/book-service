package com.eugene.book_service.service;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<Category> createCategory(CategoryDto categoryDto) {
        if (categoryRepository
                .findByName(categoryDto.name())
                .isPresent()) { // Can create the same category twice
            return ResponseEntity
                    .badRequest()
                    .build();
        } else {
            Category category = new Category(categoryDto.name());
            return ResponseEntity.ok(categoryRepository.save(category));
        }
    }

    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    public ResponseEntity<Category> getCategoryByName(String categoryName) {
        Category category = categoryRepository
                .findByName(categoryName)
                .orElse(null);

        if (category == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(category);
        }
    }
}
