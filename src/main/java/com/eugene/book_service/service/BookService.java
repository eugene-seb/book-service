package com.eugene.book_service.service;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.kafka.BookEventProducer;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.BookRepository;
import com.eugene.book_service.repository.CategoryRepository;
import com.eugene.book_service.repository.specification.BookSpecification;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookEventProducer bookEventProducer;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(
            BookEventProducer bookEventProducer, BookRepository bookRepository,
            CategoryRepository categoryRepository) {
        this.bookEventProducer = bookEventProducer;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ResponseEntity<BookDetailsDto> createBook(BookDto bookDto) throws URISyntaxException {
        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));

        if (categories.size() != bookDto
                .categoriesIds()
                .size()) { // At least one category doesn't exist
            return ResponseEntity
                    .badRequest()
                    .build();
        } else if (bookRepository.existsById(bookDto.isbn())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        } else {
            Book book = bookDto.toBook();
            book.setCategories(categories);

            BookDetailsDto bookCreated = bookRepository
                    .save(book)
                    .toBookDetailsDto();

            return ResponseEntity
                    .created(new URI("/book?isbn=" + bookCreated.isbn()))
                    .body(bookCreated);
        }
    }

    @Transactional
    public ResponseEntity<List<BookDetailsDto>> getAllBook() {
        List<BookDetailsDto> books = bookRepository
                .findAll()
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
        return ResponseEntity.ok(books);
    }

    @Transactional
    public ResponseEntity<List<BookDetailsDto>> searchBooksByKey(BookDto bookDto) {
        Specification<Book> bookSpec = BookSpecification.filterBy(bookDto);
        List<BookDetailsDto> books = bookRepository
                .findAll(bookSpec)
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
        return ResponseEntity.ok(books);
    }

    @Transactional
    public ResponseEntity<BookDetailsDto> getBookByIsbn(String isbn) {
        Book book = bookRepository
                .findById(isbn)
                .orElse(null);

        if (book == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(book.toBookDetailsDto());
        }
    }

    @Transactional
    public ResponseEntity<Boolean> doesBookExists(String isbn) {
        boolean exist = bookRepository.existsById(isbn);

        return ResponseEntity.ok(exist);
    }

    @Transactional
    public ResponseEntity<BookDetailsDto> updateBook(BookDto bookDto) {
        Book book = bookRepository
                .findById(bookDto.isbn())
                .orElse(null);
        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));

        if (book == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else if (categories.size() != bookDto
                .categoriesIds()
                .size()) { // Some categories don't exist
            return ResponseEntity
                    .badRequest()
                    .build();
        } else {
            book.setTitle(bookDto.title());
            book.setDescription(bookDto.description());
            book.setAuthor(bookDto.author());
            book.setUrl(bookDto.url());
            book.setCategories(categories);

            BookDetailsDto bookUpdated = bookRepository
                    .save(book)
                    .toBookDetailsDto();
            return ResponseEntity.ok(bookUpdated);
        }
    }

    @Transactional
    public ResponseEntity<Void> deleteBook(String isbn) {
        bookRepository
                .findById(isbn)
                .ifPresent(book -> {
                    bookRepository.deleteById(isbn);
                    try {
                        bookEventProducer.sendBookDeletedEvent(book.getReviewsIds());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e.getMessage(), e.getCause());
                    }
                });

        return ResponseEntity
                .ok()
                .build();
    }
}
