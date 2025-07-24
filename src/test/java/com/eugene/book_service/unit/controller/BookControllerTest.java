package com.eugene.book_service.unit.controller;

import com.eugene.book_service.controller.BookController;
import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
class BookControllerTest
{
    private BookDto bookDto;
    private BookDetailsDto bookDetailsDto;
    private List<BookDetailsDto> bookList;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            // Enable the support of LocalDateTime for JSON serialization/deserialization)
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void initVariables() {
        this.bookDto = new BookDto("isbn11", "new title", "String description", "String author",
                                   "String url", new HashSet<>(List.of(1L, 2L, 3L)));

        this.bookDetailsDto = new BookDetailsDto("isbn11", "new title", "String description",
                                                 "String author", "String url",
                                                 new HashSet<>(List.of("art", "music", "science")),
                                                 new HashSet<>(List.of(1L, 2L, 3L)));

        this.bookList = List.of(
                new BookDetailsDto("isbn11", "String title", "String description", "String author",
                                   "String url", new HashSet<>(), new HashSet<>()));
    }

    @Test
    @Disabled
    void createBook() throws Exception {
        given(this.bookService.createBook(this.bookDto)).willReturn(this.bookDetailsDto);

        this.mockMvc.perform(post("/book/create_book").contentType(MediaType.APPLICATION_JSON)
                                                      .content(asJsonString(this.bookDto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location",
                                               "/book?isbn=" + this.bookDetailsDto.getIsbn()))
                    .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));

        verify(this.bookService).createBook(this.bookDto);
    }

    @Test
    void getAllBook() throws Exception {

        given(this.bookService.getAllBook()).willReturn(this.bookList);

        this.mockMvc.perform(get("/book/all_books"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.size()").value(this.bookList.size()));

        verify(this.bookService).getAllBook();
    }

    @Test
    void getBookByIsbn() throws Exception {
        given(this.bookService.getBookByIsbn(this.bookDto.getIsbn())).willReturn(
                this.bookDetailsDto);

        this.mockMvc.perform(get("/book?isbn=" + this.bookDto.getIsbn()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));

        verify(this.bookService).getBookByIsbn(this.bookDto.getIsbn());
    }

    @Test
    void doesBookExist() throws Exception {
        given(this.bookService.doesBookExists(this.bookDto.getIsbn())).willReturn(true);

        this.mockMvc.perform(get("/book/exists/" + this.bookDto.getIsbn()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").value(true));

        verify(this.bookService).doesBookExists(this.bookDto.getIsbn());
    }

    @Test
    void updateBook() throws Exception {
        given(this.bookService.updateBook(any(BookDto.class))).willReturn(this.bookDetailsDto);

        this.mockMvc.perform(put("/book/update/{isbn}", this.bookDto.getIsbn()).contentType(
                                                                                       MediaType.APPLICATION_JSON)
                                                                               .content(
                                                                                       asJsonString(
                                                                                               this.bookDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));

        verify(this.bookService).updateBook(any(BookDto.class));
    }

    @Test
    void deleteBook() throws Exception {
        doNothing().when(this.bookService)
                   .deleteBook(this.bookDto.getIsbn());

        this.mockMvc.perform(delete(new URI("/book/delete/" + this.bookDto.getIsbn())))
                    .andExpect(status().isOk());

        verify(this.bookService).deleteBook(this.bookDto.getIsbn());
    }
}
