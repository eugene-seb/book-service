package com.eugene.book_service.controller;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("create_category")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDto categoryDto) {
        return categoryService.createCategory(categoryDto);
    }

    @GetMapping("all_categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping
    public ResponseEntity<Category> getCategoryByName(@RequestParam String categoryName) {
        return categoryService.getCategoryByName(categoryName);
    }
}
