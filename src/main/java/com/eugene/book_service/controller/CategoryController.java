package com.eugene.book_service.controller;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("create_category")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDto categoryDto) throws
            URISyntaxException {
        return categoryService.createCategory(categoryDto);
    }

    @GetMapping("all_categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping
    public ResponseEntity<Category> getCategoryById(@RequestParam Long idCategory) {
        return categoryService.getCategoryById(idCategory);
    }

    @PutMapping("/update/{idCategory}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long idCategory, @RequestBody CategoryDto categoryDto) {
        return categoryService.updateCategory(idCategory, categoryDto);
    }

    @DeleteMapping("delete/{idCategory}")
    public ResponseEntity<Category> deleteCategory(@PathVariable Long idCategory) {
        return categoryService.deleteCategory(idCategory);
    }
}
