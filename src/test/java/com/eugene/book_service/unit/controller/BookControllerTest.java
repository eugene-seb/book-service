package com.eugene.book_service.unit.controller;

import com.eugene.book_service.controller.BookController;
import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
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

    BookDto bookDto;
    BookDetailsDto bookDetailsDto;
    List<BookDetailsDto> bookList;

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
        bookDto = new BookDto("isbn11", "new title", "String description", "String author",
                "String url", new HashSet<>(List.of(1L, 2L, 3L)));

        bookDetailsDto = new BookDetailsDto("isbn11", "new title", "String description",
                "String author", "String url", new HashSet<>(List.of("art", "music", "science")),
                new HashSet<>(List.of(1L, 2L, 3L)));

        bookList = List.of(
                new BookDetailsDto("isbn11", "String title", "String description", "String author",
                        "String url", new HashSet<>(), new HashSet<>()));
    }

    @Test
    void createBook() throws Exception {
        given(bookService.createBook(bookDto)).willReturn(bookDetailsDto);

        mockMvc
                .perform(post("/book/create_book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/book?isbn=" + bookDetailsDto.isbn()))
                .andExpect(jsonPath("$.isbn").value(bookDetailsDto.isbn()));

        verify(bookService).createBook(bookDto);
    }

    @Test
    void getAllBook() throws Exception {

        given(bookService.getAllBook()).willReturn(bookList);

        mockMvc
                .perform(get("/book/all_books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(bookList.size()));

        verify(bookService).getAllBook();
    }

    @Test
    void getBookByIsbn() throws Exception {
        given(bookService.getBookByIsbn("isbn11")).willReturn(bookDetailsDto);

        mockMvc
                .perform(get("/book?isbn=" + bookDto.isbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(bookDetailsDto.isbn()));

        verify(bookService).getBookByIsbn(bookDto.isbn());
    }

    @Test
    void doesBookExist() throws Exception {
        given(bookService.doesBookExists(bookDto.isbn())).willReturn(true);

        mockMvc
                .perform(get("/book/exists/" + bookDto.isbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));

        verify(bookService).doesBookExists(bookDto.isbn());
    }

    @Test
    void updateBook() throws Exception {
        given(bookService.updateBook(any(BookDto.class))).willReturn(bookDetailsDto);

        mockMvc
                .perform(put("/book/update/{isbn}", bookDto.isbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(bookDetailsDto.isbn()));

        verify(bookService).updateBook(any(BookDto.class));
    }

    @Test
    void deleteBook() throws Exception {
        doNothing()
                .when(bookService)
                .deleteBook(bookDto.isbn());

        mockMvc
                .perform(delete(new URI("/book/delete/" + bookDto.isbn())))
                .andExpect(status().isOk());

        verify(bookService).deleteBook(bookDto.isbn());
    }
}
