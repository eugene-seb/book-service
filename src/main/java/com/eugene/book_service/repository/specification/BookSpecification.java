package com.eugene.book_service.repository.specification;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification
{
    private BookSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Book> filterBy(BookDto bookDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookDto.getIsbn() != null) predicates.add(
                    criteriaBuilder.like(root.get("isbn"), "%" + bookDto.getTitle() + "%"));
            if (bookDto.getTitle() != null) predicates.add(
                    criteriaBuilder.like(root.get("title"), "%" + bookDto.getTitle() + "%"));
            if (bookDto.getDescription() != null) predicates.add(
                    criteriaBuilder.like(root.get("description"),
                                         "%" + bookDto.getDescription() + "%"));
            if (bookDto.getAuthor() != null) predicates.add(
                    criteriaBuilder.like(root.get("author"), "%" + bookDto.getAuthor() + "%"));

            return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
        };
    }
}
