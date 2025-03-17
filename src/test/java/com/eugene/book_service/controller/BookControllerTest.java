package com.eugene.book_service.controller;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.model.Book;
import com.eugene.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    Book book;
    BookDto bookDto;
    List<Book> bookList;

    static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void initVariables() {
        book = new Book("isbn11", "String title", "String description", "String author",
                "String url");

        bookDto = new BookDto("isbn11", "new title", "String description", "String author",
                "String url", new HashSet<>(List.of(1L, 2L, 3L)));

        bookList = List.of(new Book("isbn11", "String title", "String description", "String author",
                "String url"));
    }

    @Test
    void createBook() throws Exception {
        given(bookService.createBook(bookDto)).willReturn(ResponseEntity
                .created(new URI("/book?isbn=" + book.getIsbn()))
                .body(book));

        mockMvc
                .perform(post("/book/create_book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/book?isbn=isbn11"))
                .andExpect(jsonPath("$.isbn").value("isbn11"));

        verify(bookService).createBook(bookDto);
    }

    @Test
    void getAllBook() throws Exception {


        given(bookService.getAllBook()).willReturn(ResponseEntity.ok(bookList));

        mockMvc
                .perform(get("/book/all_books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookList.size()));

        verify(bookService).getAllBook();
    }

    @Test
    void getBookByIsbn() throws Exception {
        given(bookService.getBookByIsbn("isbn11")).willReturn(ResponseEntity.ok(book));

        mockMvc
                .perform(get("/book?isbn=isbn11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("isbn11"));

        verify(bookService).getBookByIsbn("isbn11");
    }

    @Test
    void doesBookExist() throws Exception {
        given(bookService.doesBookExist(book.getIsbn())).willReturn(ResponseEntity.ok(true));

        mockMvc
                .perform(get("/book/exists/" + book.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(bookService).doesBookExist(book.getIsbn());
    }

    @Test
    void updateBook() throws Exception {
        given(bookService.updateBook(eq(book.getIsbn()), any(BookDto.class))).willReturn(
                ResponseEntity.ok(book));

        mockMvc
                .perform(put("/book/update/{isbn}", book.getIsbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("isbn11"));

        verify(bookService).updateBook(eq(book.getIsbn()), any(BookDto.class));
    }

    @Test
    void deleteBook() throws Exception {
        given(bookService.deleteBook(book.getIsbn())).willReturn(ResponseEntity
                .ok()
                .build());

        mockMvc
                .perform(delete(new URI("/book/delete/" + book.getIsbn())))
                .andExpect(status().isOk());

        verify(bookService).deleteBook(book.getIsbn());
    }
}
