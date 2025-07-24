package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("book")
public class BookController
{
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("create_book")
    public ResponseEntity<BookDetailsDto> createBook(@Valid @RequestBody BookDto bookDto)
            throws URISyntaxException {

        return ResponseEntity.created(new URI("/book?isbn=" + bookDto.getIsbn()))
                             .body(this.bookService.createBook(bookDto));
    }

    @GetMapping("all_books")
    public ResponseEntity<List<BookDetailsDto>> getAllBook() {
        return ResponseEntity.ok(this.bookService.getAllBook());
    }

    @GetMapping
    public ResponseEntity<BookDetailsDto> getBookByIsbn(@RequestParam String isbn) {
        return ResponseEntity.ok(this.bookService.getBookByIsbn(isbn));
    }

    @GetMapping("search")
    public ResponseEntity<List<BookDetailsDto>> searchBookByKey(@Valid @RequestBody BookDto bookDto) {
        return ResponseEntity.ok(this.bookService.searchBooksByKey(bookDto));
    }

    @GetMapping("exists/{isbn}")
    public ResponseEntity<Boolean> doesBookExist(@PathVariable String isbn) {
        return ResponseEntity.ok(this.bookService.doesBookExists(isbn));
    }

    @PutMapping("/update/{isbn}")
    public ResponseEntity<BookDetailsDto> updateBook(
            @PathVariable String isbn,
            @Valid @RequestBody BookDto bookDto
    ) {
        return ResponseEntity.ok(this.bookService.updateBook(bookDto));
    }

    @DeleteMapping("delete/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        this.bookService.deleteBook(isbn);
        return ResponseEntity.ok()
                             .build();
    }
}
