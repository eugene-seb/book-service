package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("book")
public class BookController {
    private static final String EXCEPTION_IS = "Exception is : ";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("create_book")
    public ResponseEntity<BookDetailsDto> createBook(@RequestBody BookDto bookDto) throws
            URISyntaxException {
        return bookService.createBook(bookDto);
    }

    @GetMapping("all_books")
    public ResponseEntity<List<BookDetailsDto>> getAllBook() {
        return bookService.getAllBook();
    }

    @GetMapping
    public ResponseEntity<BookDetailsDto> getBookByIsbn(@RequestParam String isbn) {
        return bookService.getBookByIsbn(isbn);
    }

    @GetMapping("search")
    public ResponseEntity<List<BookDetailsDto>> searchBookByKey(@RequestBody BookDto bookDto) {
        return bookService.searchBooksByKey(bookDto);
    }

    @GetMapping("exists/{isbn}")
    public ResponseEntity<Boolean> doesBookExist(@PathVariable String isbn) {
        return bookService.doesBookExists(isbn);
    }

    @PutMapping("/update/{isbn}")
    public ResponseEntity<BookDetailsDto> updateBook(
            @PathVariable String isbn, @RequestBody BookDto bookDto) {
        return bookService.updateBook(bookDto);
    }

    @DeleteMapping("delete/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        return bookService.deleteBook(isbn);
    }

    /**
     * Maps UnsupportedOperationException to a 501 Not Implemented HTTP status
     * code.
     */
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    @ExceptionHandler({UnsupportedOperationException.class})
    public void handleUnableToReallocate(Exception ex) {
        logger.error(EXCEPTION_IS, ex);
        // just return empty 501
    }

    /**
     * Maps IllegalArgumentExceptions to a 404 Not Found HTTP status code.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(IllegalArgumentException.class)
    public void handleNotFound(Exception ex) {
        logger.error(EXCEPTION_IS, ex);
        // return empty 404
    }

    /**
     * Maps DataIntegrityViolationException to a 409 Conflict HTTP status code.
     */
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler({DataIntegrityViolationException.class})
    public void handleAlreadyExists(Exception ex) {
        logger.error(EXCEPTION_IS, ex);
        // return empty 409
    }
}
