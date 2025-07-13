package com.eugene.book_service.service;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.exception.DuplicatedException;
import com.eugene.book_service.exception.NotFoundException;
import com.eugene.book_service.kafka.BookEventProducer;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.repository.BookRepository;
import com.eugene.book_service.repository.CategoryRepository;
import com.eugene.book_service.repository.specification.BookSpecification;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private static String getBookNotFoundMessage(String isbn) {
        return "Book with ISBN '" + isbn + "' not found.";
    }

    @Transactional
    public BookDetailsDto createBook(BookDto bookDto) {
        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));

        if (categories.size() != bookDto
                .categoriesIds()
                .size()) {
            throw new IllegalArgumentException("At least one category doesn't exist");
        } else if (bookRepository.existsById(bookDto.isbn())) {
            throw new DuplicatedException(
                    "Book with ISBN '" + bookDto.isbn() + "' " + "already exists.", null);
        } else {
            Book book = bookDto.toBook();
            book.setCategories(categories);

            return bookRepository
                    .save(book)
                    .toBookDetailsDto();
        }
    }

    @Transactional
    public List<BookDetailsDto> getAllBook() {
        return bookRepository
                .findAll()
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
    }

    @Transactional
    public List<BookDetailsDto> searchBooksByKey(BookDto bookDto) {
        Specification<Book> bookSpec = BookSpecification.filterBy(bookDto);
        return bookRepository
                .findAll(bookSpec)
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
    }

    @Transactional
    public BookDetailsDto getBookByIsbn(String isbn) {
        return bookRepository
                .findById(isbn)
                .map(Book::toBookDetailsDto)
                .orElseThrow(() -> new NotFoundException(getBookNotFoundMessage(isbn), null));
    }

    @Transactional
    public Boolean doesBookExists(String isbn) {
        return bookRepository.existsById(isbn);
    }

    @Transactional
    public BookDetailsDto updateBook(BookDto bookDto) {

        Book book = bookRepository
                .findById(bookDto.isbn())
                .orElseThrow(
                        () -> new NotFoundException(getBookNotFoundMessage(bookDto.isbn()), null));
        Set<Category> categories = new HashSet<>(
                categoryRepository.findAllById(bookDto.categoriesIds()));

        if (categories.size() != bookDto
                .categoriesIds()
                .size()) {
            throw new IllegalArgumentException("At least one category doesn't exist");
        } else {
            book.setTitle(bookDto.title());
            book.setDescription(bookDto.description());
            book.setAuthor(bookDto.author());
            book.setUrl(bookDto.url());
            book.setCategories(categories);

            return bookRepository
                    .save(book)
                    .toBookDetailsDto();
        }
    }

    @Transactional
    public void deleteBook(String isbn) {
        bookRepository
                .findById(isbn)
                .ifPresent(book -> {
                    bookRepository.deleteById(isbn);
                    bookEventProducer.sendBookDeletedEvent(book.getReviewsIds());
                });
    }
}
