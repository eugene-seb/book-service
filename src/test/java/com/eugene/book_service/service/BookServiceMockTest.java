package com.eugene.book_service.service;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.dto.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookServiceMockTest {

    private final CategoryDto categoryDto;
    private final BookDto bookDto;
    private final BookDto bookDtoNew;
    private final BookDto bookDtoFilter;


    /// I don't want the context to load kafka for this test, so I'm mocking his initialization
    /// It will be used in each function of service that call Kafka producer/consumer
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private MockMvc mockMvc;

    public BookServiceMockTest() {
        this.categoryDto = new CategoryDto("art");
        this.bookDto = new BookDto("isbn11", "title", "String description", "String author",
                "String url", new HashSet<>(List.of(1L)));
        this.bookDtoNew = new BookDto("isbn11", "new title", "String description", "String author",
                "String url", new HashSet<>(List.of(1L)));
        this.bookDtoFilter = new BookDto("isbn11", null, "String description", "String author",
                null, new HashSet<>(List.of()));
    }

    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Order(1)
    void createBook() throws Exception {
        mockMvc
                .perform(post("/category/create_category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/category?idCategory=1"))
                .andExpect(jsonPath("$.name").value("art"));

        mockMvc
                .perform(post("/book/create_book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/book?isbn=isbn11"))
                .andExpect(jsonPath("$.isbn").value("isbn11"));

        mockMvc
                .perform(post("/book/create_book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDto)))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(2)
    void getAllBook() throws Exception {

        mockMvc
                .perform(get("/book/all_books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(3)
    void getBookByIsbn() throws Exception {

        mockMvc
                .perform(get("/book?isbn={isbn}", bookDto.isbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("isbn11"));
    }

    @Test
    @Order(4)
    void doesBookExist() throws Exception {

        mockMvc
                .perform(get("/book/exists/{isbn}", bookDto.isbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }

    @Test
    @Order(5)
    void updateBook() throws Exception {

        mockMvc
                .perform(put("/book/update/{isbn}", bookDto.isbn())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDtoNew)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(bookDtoNew.title()));
    }

    @Test
    @Order(6)
    void searchBookByKey() throws Exception {

        mockMvc
                .perform(get("/book/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(bookDtoFilter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(7)
    void deleteBook() throws Exception {

        mockMvc
                .perform(delete(new URI("/book/delete/" + bookDto.isbn())))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/book?isbn={isbn}", bookDto.isbn()))
                .andExpect(status().isNotFound());
    }
}
