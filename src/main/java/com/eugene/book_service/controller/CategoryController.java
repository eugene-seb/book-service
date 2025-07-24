package com.eugene.book_service.controller;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("category")
public class CategoryController
{
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("create_category")
    public ResponseEntity<Category> createCategory(@Valid @RequestBody CategoryDto categoryDto)
            throws URISyntaxException {
        Category category = this.categoryService.createCategory(categoryDto);
        return ResponseEntity.created(new URI("/category?idCategory=" + category.getId()))
                             .body(category);
    }

    @GetMapping("all_categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(this.categoryService.getAllCategories());
    }

    @GetMapping
    public ResponseEntity<Category> getCategoryById(@RequestParam Long idCategory) {
        return ResponseEntity.ok(this.categoryService.getCategoryById(idCategory));
    }

    @PutMapping("/update/{idCategory}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long idCategory,
            @Valid @RequestBody CategoryDto categoryDto
    ) {
        return ResponseEntity.ok(this.categoryService.updateCategory(idCategory, categoryDto));
    }

    @DeleteMapping("delete/{idCategory}")
    public ResponseEntity<Category> deleteCategory(@PathVariable Long idCategory) {

        this.categoryService.deleteCategory(idCategory);

        return ResponseEntity.ok()
                             .build();
    }
}
