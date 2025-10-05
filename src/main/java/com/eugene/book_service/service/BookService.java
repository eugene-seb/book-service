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
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BookService
{
    private final BookEventProducer bookEventProducer;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    
    private static String getBookNotFoundMessage(String isbn) {
        return "Book with ISBN '" + isbn + "' not found.";
    }
    
    @Transactional
    public BookDetailsDto createBook(BookDto bookDto) {
        Set<Category> categories = new HashSet<>(this.categoryRepository.findAllById(bookDto.getCategoriesIds()));
        
        if (categories.size() != bookDto
                .getCategoriesIds()
                .size()) {
            throw new IllegalArgumentException("At least one category doesn't exist");
        } else if (this.bookRepository.existsById(bookDto.getIsbn())) {
            throw new DuplicatedException(
                    "Book with ISBN '" + bookDto.getIsbn() + "' " + "already exists.",
                    null);
        } else {
            Book book = bookDto.toBook();
            book.setCategories(categories);
            
            return this.bookRepository
                    .save(book)
                    .toBookDetailsDto();
        }
    }
    
    @Transactional(readOnly = true)
    public List<BookDetailsDto> getAllBook() {
        return this.bookRepository
                .findAll()
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public List<BookDetailsDto> searchBooksByKey(BookDto bookDto) {
        Specification<Book> bookSpec = BookSpecification.filterBy(bookDto);
        return this.bookRepository
                .findAll(bookSpec)
                .stream()
                .map(Book::toBookDetailsDto)
                .toList();
    }
    
    @Transactional(readOnly = true)
    public BookDetailsDto getBookByIsbn(String isbn) {
        return this.bookRepository
                .findById(isbn)
                .map(Book::toBookDetailsDto)
                .orElseThrow(() -> new NotFoundException(getBookNotFoundMessage(isbn),
                                                         null));
    }
    
    @Transactional(readOnly = true)
    public Boolean doesBookExists(String isbn) {
        return this.bookRepository.existsById(isbn);
    }
    
    @Transactional
    public BookDetailsDto updateBook(
            String isbn,
            BookDto bookDto
    ) {
        
        Book book = this.bookRepository
                .findById(isbn)
                .orElseThrow(() -> new NotFoundException(getBookNotFoundMessage(bookDto.getIsbn()),
                                                         null));
        Set<Category> categories = new HashSet<>(this.categoryRepository.findAllById(bookDto.getCategoriesIds()));
        
        if (categories.size() != bookDto
                .getCategoriesIds()
                .size()) {
            throw new IllegalArgumentException("At least one category doesn't exist");
        } else {
            book.setTitle(bookDto.getTitle());
            book.setDescription(bookDto.getDescription());
            book.setAuthor(bookDto.getAuthor());
            book.setUrl(bookDto.getUrl());
            book.setCategories(categories);
            
            return this.bookRepository
                    .save(book)
                    .toBookDetailsDto();
        }
    }
    
    @Transactional
    public void deleteBook(String isbn) {
        Book book = this.bookRepository
                .findById(isbn)
                .orElseThrow(() -> new NotFoundException(getBookNotFoundMessage(isbn),
                                                         null));
        
        this.bookRepository.delete(book);
        this.bookEventProducer.sendBookDeletedEvent(book.getReviewsIds());
    }
}
