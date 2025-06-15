package com.eugene.book_service.dto;

import com.eugene.book_service.model.Book;

import java.util.Set;

/**
 * This class is used to create a book.
 *
 * @param isbn          the ISBN of the book
 * @param title         the title of the book
 * @param description   the description of the book
 * @param author        the author of the book
 * @param url           the URL of the book cover image
 * @param categoriesIds the IDs of categories associated with the book
 */
public record BookDto(String isbn, String title, String description, String author, String url,
                      Set<Long> categoriesIds) {
    public Book toBook() {
        return new Book(isbn, title, description, author, url);
    }
}
