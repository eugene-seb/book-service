package com.eugene.book_service.unit.controller;

import com.eugene.book_service.config.SecurityConfig;
import com.eugene.book_service.controller.BookController;
import com.eugene.book_service.dto.BookDetailsDto;
import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BookController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
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
        this.bookDto = new BookDto("isbn11",
                                   "new title",
                                   "String description",
                                   "String author",
                                   "String url",
                                   new HashSet<>(List.of(1L,
                                                         2L,
                                                         3L)));
        
        this.bookDetailsDto = new BookDetailsDto("isbn11",
                                                 "new title",
                                                 "String description",
                                                 "String author",
                                                 "String url",
                                                 new HashSet<>(List.of("art",
                                                                       "music",
                                                                       "science")),
                                                 new HashSet<>(List.of(1L,
                                                                       2L,
                                                                       3L)));
        
        this.bookList = List.of(new BookDetailsDto("isbn11",
                                                   "String title",
                                                   "String description",
                                                   "String author",
                                                   "String url",
                                                   new HashSet<>(),
                                                   new HashSet<>()));
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void createBook() throws Exception {
        given(this.bookService.createBook(any(BookDto.class))).willReturn(this.bookDetailsDto);
        
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/book/" + this.bookDetailsDto.getIsbn()))
                .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));
        
        verify(this.bookService).createBook(any(BookDto.class));
    }
    
    @Test
    @WithMockUser
    void createBook_shouldBeForbiddenForUserRole() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser
    void getAllBook() throws Exception {
        
        given(this.bookService.getAllBook()).willReturn(this.bookList);
        
        this.mockMvc
                .perform(get("/api/book/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(this.bookList.size()));
        
        verify(this.bookService).getAllBook();
    }
    
    @Test
    void getAllBook_shouldBeUnauthorizedWithoutAuthentication() throws Exception {
        this.mockMvc
                .perform(get("/api/book/all"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser
    void getBookByIsbn() throws Exception {
        given(this.bookService.getBookByIsbn(this.bookDto.getIsbn())).willReturn(this.bookDetailsDto);
        
        this.mockMvc
                .perform(get("/api/book/" + this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));
        
        verify(this.bookService).getBookByIsbn(this.bookDto.getIsbn());
    }
    
    @Test
    @WithMockUser
    void searchBookByKey() throws Exception {
        given(this.bookService.searchBooksByKey(any(BookDto.class))).willReturn(this.bookList);
        
        this.mockMvc
                .perform(post("/api/book/search")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(this.bookList.size()));
        
        verify(this.bookService).searchBooksByKey(any(BookDto.class));
    }
    
    @Test
    @WithMockUser
    void doesBookExist() throws Exception {
        given(this.bookService.doesBookExists(this.bookDto.getIsbn())).willReturn(true);
        
        this.mockMvc
                .perform(get("/api/book/exists/" + this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
        
        verify(this.bookService).doesBookExists(this.bookDto.getIsbn());
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void updateBook() throws Exception {
        given(this.bookService.updateBook(anyString(),
                                          any(BookDto.class))).willReturn(this.bookDetailsDto);
        
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             this.bookDto.getIsbn())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(this.bookDetailsDto.getIsbn()));
        
        verify(this.bookService).updateBook(anyString(),
                                            any(BookDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteBook() throws Exception {
        doNothing()
                .when(this.bookService)
                .deleteBook(this.bookDto.getIsbn());
        
        this.mockMvc
                .perform(delete("/api/book/delete/" + this.bookDto.getIsbn()).with(csrf()))
                .andExpect(status().isNoContent());
        
        verify(this.bookService).deleteBook(this.bookDto.getIsbn());
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR"})
    void deleteBook_shouldBeForbiddenForModeratorRole() throws Exception {
        this.mockMvc
                .perform(delete("/api/book/delete/" + this.bookDto.getIsbn()).with(csrf()))
                .andExpect(status().isForbidden());
    }
}