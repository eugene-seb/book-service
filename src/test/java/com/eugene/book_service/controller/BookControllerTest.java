package com.eugene.book_service.controller;

import com.eugene.book_service.model.Book;
import com.eugene.book_service.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
public class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    BookService bookService;

    @Test
    void getAllBook() throws Exception {
        List<Book> books = List.of(new Book("String isbn", "String title", "String description", "String author", "String url"));

        given(bookService
                .getAllBook())
                .willReturn(ResponseEntity.ok(books));

        mockMvc
                .perform(get("/book/all_books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(books.size()));

        verify(bookService).getAllBook();
    }
}
