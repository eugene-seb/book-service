package com.eugene.book_service.controller;

import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController
{
    private final CategoryService categoryService;
    
    @Operation(summary = "Create a new category of book.")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Category> createCategory(@RequestBody CategoryDto categoryDto)
            throws URISyntaxException {
        Category category = this.categoryService.createCategory(categoryDto);
        return ResponseEntity
                .created(new URI("/api/category/" + category.getId()))
                .body(category);
    }
    
    @Operation(summary = "Get all categories of book.")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(this.categoryService.getAllCategories());
    }
    
    @Operation(summary = "Get a category of book by ID.")
    @GetMapping("/{idCategory}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long idCategory) {
        return ResponseEntity.ok(this.categoryService.getCategoryById(idCategory));
    }
    
    @Operation(summary = "Update a category of book.")
    @PutMapping("/update/{idCategory}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<Category> updateCategory(
            @PathVariable Long idCategory,
            @RequestBody CategoryDto categoryDto
    ) {
        return ResponseEntity.ok(this.categoryService.updateCategory(idCategory,
                                                                     categoryDto));
    }
    
    @Operation(summary = "Delete a category of book.")
    @DeleteMapping("/delete/{idCategory}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Category> deleteCategory(@PathVariable Long idCategory) {
        
        this.categoryService.deleteCategory(idCategory);
        
        return ResponseEntity
                .ok()
                .build();
    }
}
