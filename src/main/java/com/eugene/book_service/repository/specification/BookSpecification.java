package com.eugene.book_service.repository.specification;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class BookSpecification {

    private BookSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<Book> filterBy(BookDto bookDto) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (bookDto.isbn() != null)
                predicates.add(criteriaBuilder.like(root.get("isbn"), "%" + bookDto.isbn() + "%"));
            if (bookDto.title() != null) predicates.add(
                    criteriaBuilder.like(root.get("title"), "%" + bookDto.title() + "%"));
            if (bookDto.description() != null) predicates.add(
                    criteriaBuilder.like(root.get("description"),
                            "%" + bookDto.description() + "%"));
            if (bookDto.author() != null) predicates.add(
                    criteriaBuilder.like(root.get("author"), "%" + bookDto.author() + "%"));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
