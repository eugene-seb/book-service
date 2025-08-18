package com.eugene.book_service.dto;

import com.eugene.book_service.model.Book;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * This class is used to create a book.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookDto
{
    @NotBlank(message = "The ISBN is required.")
    private String isbn;

    @NotNull(message = "The title is required.")
    private String title;

    private String description;

    @NotBlank(message = "The author is required.")
    private String author;

    @NotBlank(message = "The location of the book is required.")
    private String url;

    private Set<Long> categoriesIds;

    public Book toBook() {
        return new Book(isbn, title, description, author, url);
    }
}