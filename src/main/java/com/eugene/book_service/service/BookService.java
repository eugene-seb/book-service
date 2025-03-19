package com.eugene.book_service.service;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.BookRepository;
import com.eugene.book_service.repository.CategoryRepository;
import com.eugene.book_service.repository.specification.BookSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookService(
            BookRepository bookRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    public ResponseEntity<Book> createBook(BookDto bookDto) throws URISyntaxException {
        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));

        if (categories.size() != bookDto
                .categoriesIds()
                .size()) { // At least one category doesn't exist
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        if (bookRepository.existsById(bookDto.isbn())) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .build();
        }

        Book book = new Book(bookDto.isbn(), bookDto.title(), bookDto.description(),
                bookDto.author(), bookDto.url());
        book.setCategories(categories);

        Book bookCreated = bookRepository.save(book);

        return ResponseEntity
                .created(new URI("/book?isbn=" + bookCreated.getIsbn()))
                .body(bookCreated);
    }

    public ResponseEntity<List<Book>> getAllBook() {
        List<Book> books = bookRepository.findAll();
        return ResponseEntity.ok(books);
    }

    public ResponseEntity<List<Book>> searchBooksByKey(BookDto bookDto) {
        Specification<Book> bookSpec = BookSpecification.filterBy(bookDto);
        List<Book> books = bookRepository.findAll(bookSpec);
        return ResponseEntity.ok(books);
    }

    public ResponseEntity<Book> getBookByIsbn(String isbn) {
        Book book = bookRepository
                .findById(isbn)
                .orElse(null);

        if (book == null) {
            return ResponseEntity
                    .notFound()
                    .build();
        } else {
            return ResponseEntity.ok(book);
        }
    }

    public ResponseEntity<Boolean> doesBookExist(String isbn) {
        boolean exist = bookRepository.existsById(isbn);

        return ResponseEntity.ok(exist);
    }

    public ResponseEntity<Book> updateBook(String isbn, BookDto bookDto) {
        Optional<Book> existingBookOpt = bookRepository.findById(isbn);

        if (existingBookOpt.isEmpty()) {
            return ResponseEntity
                    .notFound()
                    .build();
        }

        Book bookOld = existingBookOpt.get();
        bookOld.setTitle(bookDto.title());
        bookOld.setDescription(bookDto.description());
        bookOld.setAuthor(bookDto.author());
        bookOld.setUrl(bookDto.url());

        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));
        if (categories.size() != bookDto
                .categoriesIds()
                .size()) { // Some categories don't exist
            return ResponseEntity
                    .badRequest()
                    .build();
        }
        bookOld.setCategories(categories);

        Book bookUpdated = bookRepository.save(bookOld);
        return ResponseEntity.ok(bookUpdated);
    }

    public ResponseEntity<Book> deleteBook(String isbn) {
        bookRepository.deleteById(isbn);
        return ResponseEntity
                .ok()
                .build();
    }
}
