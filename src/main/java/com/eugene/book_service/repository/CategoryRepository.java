package com.eugene.book_service.repository;

import com.eugene.book_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long>
{
    Optional<Category> findByName(String categoryName);
}
