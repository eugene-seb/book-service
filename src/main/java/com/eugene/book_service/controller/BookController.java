package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("book")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("create_book")
    public ResponseEntity<BookDetailsDto> createBook(@RequestBody BookDto bookDto) throws
            URISyntaxException {

        return ResponseEntity
                .created(new URI("/book?isbn=" + bookDto.isbn()))
                .body(bookService.createBook(bookDto));
    }

    @GetMapping("all_books")
    public ResponseEntity<List<BookDetailsDto>> getAllBook() {
        return ResponseEntity.ok(bookService.getAllBook());
    }

    @GetMapping
    public ResponseEntity<BookDetailsDto> getBookByIsbn(@RequestParam String isbn) {
        return ResponseEntity.ok(bookService.getBookByIsbn(isbn));
    }

    @GetMapping("search")
    public ResponseEntity<List<BookDetailsDto>> searchBookByKey(@RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.searchBooksByKey(bookDto));
    }

    @GetMapping("exists/{isbn}")
    public ResponseEntity<Boolean> doesBookExist(@PathVariable String isbn) {
        return ResponseEntity.ok(bookService.doesBookExists(isbn));
    }

    @PutMapping("/update/{isbn}")
    public ResponseEntity<BookDetailsDto> updateBook(
            @PathVariable String isbn, @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(bookService.updateBook(bookDto));
    }

    @DeleteMapping("delete/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return ResponseEntity
                .ok()
                .build();
    }
}
