package com.eugene.book_service.functional;

import com.eugene.book_service.dto.BookDto;
import com.eugene.book_service.dto.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BookControllerFunctionalTest
{
    private final CategoryDto categoryDto;
    private final BookDto bookDto;
    private final BookDto bookDtoNew;
    private final BookDto bookDtoFilter;
    
    /**
     * I don't want the context to load kafka for this test, so I'm mocking his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private MockMvc mockMvc;
    
    public BookControllerFunctionalTest() {
        this.categoryDto = new CategoryDto("art");
        this.bookDto = new BookDto("isbn11",
                                   "title",
                                   "String description",
                                   "String author",
                                   "String url",
                                   new HashSet<>(List.of(1L)));
        this.bookDtoNew = new BookDto("isbn11",
                                      "new title",
                                      "String description",
                                      "String author",
                                      "String url",
                                      new HashSet<>(List.of(1L)));
        this.bookDtoFilter = new BookDto("isbn11",
                                         "",
                                         "String description",
                                         "String author",
                                         "http://location.com/book",
                                         new HashSet<>(List.of()));
    }
    
    private static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            // Enable the support of LocalDateTime for JSON serialization/deserialization)
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    @Order(1)
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void createBook() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/category/1"))
                .andExpect(jsonPath("$.name").value("art"));
        
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/book/" + this.bookDto.getIsbn()))
                .andExpect(jsonPath("$.isbn").value(this.bookDto.getIsbn()));
        
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(2)
    @WithMockUser
    void getAllBook() throws Exception {
        
        this.mockMvc
                .perform(get("/api/book/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(3)
    @WithMockUser
    void getBookByIsbn() throws Exception {
        
        this.mockMvc
                .perform(get("/api/book/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(this.bookDto.getIsbn()));
    }
    
    @Test
    @Order(4)
    @WithMockUser
    void doesBookExist() throws Exception {
        
        this.mockMvc
                .perform(get("/api/book/exists/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
    
    @Test
    @Order(6)
    @WithMockUser
    void searchBookByKey() throws Exception {
        
        this.mockMvc
                .perform(get("/api/book/search")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoFilter)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void updateBook() throws Exception {
        
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             this.bookDto.getIsbn())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoNew)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(this.bookDtoNew.getTitle()));
    }
    
    @Test
    @Order(7)
    @WithMockUser(roles = {"ADMIN"})
    void deleteBook() throws Exception {
        
        this.mockMvc
                .perform(delete(new URI("/api/book/delete/" + this.bookDto.getIsbn())))
                .andExpect(status().isNoContent());
        
        this.mockMvc
                .perform(get("/api/book/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isNotFound());
    }
}
