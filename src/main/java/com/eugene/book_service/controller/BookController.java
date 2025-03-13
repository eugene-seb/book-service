package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.service.BookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("book")
public class BookController {
    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping("create_book")
    public ResponseEntity<Book> createBook(@RequestBody BookDto bookDto) {
        return bookService.createBook(bookDto);
    }

    @GetMapping("all_books")
    public ResponseEntity<List<Book>> getAllBook() {
        return bookService.getAllBook();
    }

    @GetMapping
    public ResponseEntity<Book> getBookByIsbn(@RequestParam String isbn) {
        return bookService.getBookByIsbn(isbn);
    }

    @GetMapping("exists/{isbn}")
    public ResponseEntity<Boolean> isBookExist(@PathVariable String isbn) {
        return bookService.isBookExist(isbn);
    }
}
