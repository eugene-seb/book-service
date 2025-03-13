package com.eugene.book_service.service;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.BookRepository;
import com.eugene.book_service.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<Book> createBook(BookDto bookDto) {
        Set<Category> categories = new HashSet<>(categoryRepository.findAllById(bookDto.categoriesIds()));

        if (categories.size() != bookDto
                .categoriesIds()
                .size()) { // At least one category doesn't exist
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        Book book = new Book(bookDto.isbn(), bookDto.title(), bookDto.description(), bookDto.author(), bookDto.url());
        book.setCategories(categories);

        return ResponseEntity.ok(bookRepository.save(book));
    }


    public ResponseEntity<List<Book>> getAllBook() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    public ResponseEntity<Book> getBookByIsbn(String isbn) {
        Book book = bookRepository
                .findByIsbn(isbn)
                .orElse(null);

        if (book == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity
                    .ok(book);
        }
    }

    public ResponseEntity<Boolean> isBookExist(String isbn) {
        boolean exist = bookRepository.existsById(isbn);

        return ResponseEntity.ok(exist);
    }
}
