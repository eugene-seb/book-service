package com.eugene.book_service.dto;

import java.util.Set;

public record BookDto(
        String isbn,
        String title,
        String description,
        String author,
        String url,
        Set<Long> categoriesIds
        // No review ids
) {
}
