package com.eugene.book_service.service;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<Category> createCategory(CategoryDto categoryDto) throws
            URISyntaxException {
        if (categoryRepository
                .findByName(categoryDto.name())
                .isPresent()) { // Can create the same category twice
            return ResponseEntity
                    .badRequest()
                    .build();
        } else {
            Category category = new Category(categoryDto.name());
            Category categoryCreated = categoryRepository.save(category);

            return ResponseEntity
                    .created(new URI("/category?idCategory=" + categoryCreated.getId()))
                    .body(categoryCreated);
        }
    }

    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return ResponseEntity.ok(categories);
    }

    public ResponseEntity<Category> getCategoryById(Long idCategory) {
        Category category = categoryRepository
                .findById(idCategory)
                .orElse(null);

        if (category == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(category);
        }
    }

    public ResponseEntity<Category> updateCategory(Long idCategory, CategoryDto categoryDto) {
        Optional<Category> existingCategoryOpt = categoryRepository.findById(idCategory);

        if (existingCategoryOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        Category categoryOld = existingCategoryOpt.get();
        categoryOld.setName(categoryDto.name());
        Category categoryUpdated = categoryRepository.save(categoryOld);

        return ResponseEntity.ok(categoryUpdated);
    }

    public ResponseEntity<Category> deleteCategory(Long idCategory) {
        categoryRepository.deleteById(idCategory);
        return ResponseEntity
                .ok()
                .build();
    }
}
