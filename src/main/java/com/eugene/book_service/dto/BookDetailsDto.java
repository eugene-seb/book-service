package com.eugene.book_service.dto;

import java.util.Set;

/**
 * This class is used to transfer all the information about a book.
 *
 * @param isbn        the ISBN of the book
 * @param title       the title of the book
 * @param description the description of the book
 * @param author      the author of the book
 * @param url         the URL of the book cover image
 * @param categories  the IDs of categories associated with the book
 */
public record BookDetailsDto(String isbn, String title, String description, String author,
                             String url, Set<String> categories, Set<Long> reviewsIds) {
}
