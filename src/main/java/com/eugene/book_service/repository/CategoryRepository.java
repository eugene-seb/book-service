package com.eugene.book_service.repository;

import com.eugene.book_service.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long>
{
    
    @Query("SELECT c FROM Category c WHERE c.name = :categoryName")
    Optional<Category> findByName(@Param("categoryName") String categoryName);
}
