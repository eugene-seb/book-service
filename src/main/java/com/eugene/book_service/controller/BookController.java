package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
@RequestMapping("/api/book")
@RequiredArgsConstructor
public class BookController
{
    private final BookService bookService;
    
    @Operation(summary = "Create a new book.")
    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<BookDetailsDto> createBook(@RequestBody BookDto bookDto)
            throws URISyntaxException {
        
        return ResponseEntity
                .created(new URI("/api/book/" + bookDto.getIsbn()))
                .body(this.bookService.createBook(bookDto));
    }
    
    @Operation(summary = "Get all books.")
    @GetMapping("/all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookDetailsDto>> getAllBook() {
        return ResponseEntity.ok(this.bookService.getAllBook());
    }
    
    @Operation(summary = "Get a book ISBN.")
    @GetMapping("/{isbn}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookDetailsDto> getBookByIsbn(@PathVariable String isbn) {
        return ResponseEntity.ok(this.bookService.getBookByIsbn(isbn));
    }
    
    @Operation(summary = "Search book by ISBN.")
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookDetailsDto>> searchBookByKey(@RequestBody BookDto bookDto) {
        return ResponseEntity.ok(this.bookService.searchBooksByKey(bookDto));
    }
    
    @Operation(summary = "Check the existence of a book.")
    @GetMapping("/exists/{isbn}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Boolean> doesBookExist(@PathVariable String isbn) {
        return ResponseEntity.ok(this.bookService.doesBookExists(isbn));
    }
    
    @Operation(summary = "Update a book.")
    @PutMapping("/update/{isbn}")
    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    public ResponseEntity<BookDetailsDto> updateBook(
            @PathVariable String isbn,
            @RequestBody BookDto bookDto
    ) {
        return ResponseEntity.ok(this.bookService.updateBook(bookDto));
    }
    
    @Operation(summary = "Delete a book.")
    @DeleteMapping("/delete/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {
        this.bookService.deleteBook(isbn);
        return ResponseEntity
                .noContent()
                .build();
    }
}
