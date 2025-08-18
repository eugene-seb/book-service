package com.eugene.book_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

/**
 * This class is used to transfer all the information about a book.
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BookDetailsDto
{
    @NotBlank(message = "The ISBN is required.")
    private String isbn;

    @NotBlank(message = "The title is required.")
    private String title;

    private String description;

    @NotBlank(message = "The author is required.")
    private String author;

    @NotBlank(message = "The location of the book is required.")
    private String url;

    private Set<String> categories;
    private Set<Long> reviewsIds;
}