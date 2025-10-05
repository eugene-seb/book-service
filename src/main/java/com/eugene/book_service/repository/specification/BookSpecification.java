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
                .or(filterByAuthor(bookDto.getAuthor()));
    }
    
    public static Specification<Book> filterByIsbn(String isbn) {
        return ((root, query, criteriaBuilder) -> (isbn != null && !isbn.isBlank())
                ? criteriaBuilder.like(root.get("isbn"),
                                       "%" + isbn + "%")
                : criteriaBuilder.disjunction());
    }
    
    public static Specification<Book> filterByTitle(String title) {
        return ((root, query, criteriaBuilder) -> (title != null && !title.isBlank())
                ? criteriaBuilder.like(root.get("title"),
                                       "%" + title + "%")
                : criteriaBuilder.disjunction());
    }
    
    public static Specification<Book> filterByAuthor(String author) {
        return ((root, query, criteriaBuilder) -> (author != null && !author.isBlank())
                ? criteriaBuilder.like(root.get("author"),
                                       "%" + author + "%")
                : criteriaBuilder.disjunction());
    }
}
