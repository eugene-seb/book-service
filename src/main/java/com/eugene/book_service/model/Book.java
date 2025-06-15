package com.eugene.book_service.model;

import com.eugene.book_service.dto.BookDetailsDto;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "book")
@Setter
@Getter
@NoArgsConstructor
public class Book {
    @Id
    @Column(unique = true, nullable = false)
    private String isbn;

    @Column(nullable = false)
    private String title;

    private String description;
    private String author;

    @Column(unique = true, nullable = false)
    private String url;

    @ManyToMany
    @JoinTable(name = "book_category", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories;

    @ElementCollection
    private Set<Long> reviewsIds;

    public Book(String isbn, String title, String description, String author, String url) {
        this.isbn = isbn;
        this.title = title;
        this.description = description;
        this.author = author;
        this.url = url;
        this.categories = new HashSet<>();
        this.reviewsIds = new HashSet<>();
    }

    public BookDetailsDto toBookDetailsDto() {
        Set<String> categoriesName = new HashSet<>();
        this.categories.forEach(category -> categoriesName.add(category.getName()));

        return new BookDetailsDto(this.isbn, this.title, this.description, this.author, this.url,
                categoriesName, this.reviewsIds);
    }
}
