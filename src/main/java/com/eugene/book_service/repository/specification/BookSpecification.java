package com.eugene.book_service.repository.specification;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification
{
    private BookSpecification() {
        throw new IllegalStateException("Utility class");
    }
    
    public static Specification<Book> filterBy(BookDto bookDto) {
        return Specification
                .where(filterByIsbn(bookDto.getIsbn()))
                .or(filterByTitle(bookDto.getTitle()))
                .or(filterByDescription(bookDto.getDescription()))
                .or(filterByAuthor(bookDto.getAuthor()));
    }
    
    public static Specification<Book> filterByIsbn(String isbn) {
        return ((root, query, criteriaBuilder) -> isbn != null
                ? criteriaBuilder.like(root.get("isbn"),
                                       "%" + isbn + "%")
                : criteriaBuilder.conjunction());
    }
    
    public static Specification<Book> filterByTitle(String title) {
        return ((root, query, criteriaBuilder) -> title != null
                ? criteriaBuilder.like(root.get("title"),
                                       "%" + title + "%")
                : criteriaBuilder.conjunction());
    }
    
    public static Specification<Book> filterByDescription(String description) {
        return ((root, query, criteriaBuilder) -> description != null
                ? criteriaBuilder.like(root.get("description"),
                                       "%" + description + "%")
                : criteriaBuilder.conjunction());
    }
    
    public static Specification<Book> filterByAuthor(String author) {
        return ((root, query, criteriaBuilder) -> author != null
                ? criteriaBuilder.like(root.get("author"),
                                       "%" + author + "%")
                : criteriaBuilder.conjunction());
    }
}
