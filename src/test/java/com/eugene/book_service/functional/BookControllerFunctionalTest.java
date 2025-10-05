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

import java.util.HashSet;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    private final CategoryDto categoryDto2;
    private final BookDto bookDto;
    private final BookDto bookDtoNew;
    private final BookDto bookDtoFilter;
    private final BookDto bookDtoWithInvalidCategories;
    
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
        this.categoryDto2 = new CategoryDto("science");
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
                                         "title",
                                         "String description",
                                         "String author",
                                         "http://location.com/book",
                                         new HashSet<>(List.of()));
        this.bookDtoWithInvalidCategories = new BookDto("isbn22",
                                                        "title",
                                                        "description",
                                                        "author",
                                                        "url",
                                                        new HashSet<>(List.of(999L))); // Non-existing category
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
    void createBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(2)
    @WithMockUser(roles = {"USER"})
    void createBook_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(3)
    @WithMockUser(roles = {"MODERATOR"})
    void setupCategories() throws Exception {
        // Create first category
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isCreated());
        
        // Create second category
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto2))
                                 .with(csrf()))
                .andExpect(status().isCreated());
    }
    
    @Test
    @Order(4)
    @WithMockUser(roles = {"MODERATOR"})
    void createBook_withModeratorRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/book/" + this.bookDto.getIsbn()))
                .andExpect(jsonPath("$.isbn").value(this.bookDto.getIsbn()))
                .andExpect(jsonPath("$.title").value(this.bookDto.getTitle()))
                .andExpect(jsonPath("$.categories.size()").value(1));
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = {"MODERATOR"})
    void createBook_withDuplicateIsbn_shouldReturnConflict() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDto))
                                 .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(6)
    @WithMockUser(roles = {"MODERATOR"})
    void createBook_withInvalidCategories_shouldReturnBadRequest() throws Exception {
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoWithInvalidCategories))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(7)
    void getAllBook_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/book/all"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(8)
    @WithMockUser
    void getAllBook_withAuthenticatedUser_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/book/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].isbn").value(this.bookDto.getIsbn()))
                .andExpect(jsonPath("$[0].title").value(this.bookDto.getTitle()));
    }
    
    @Test
    @Order(9)
    @WithMockUser
    void getBookByIsbn_withValidIsbn_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/book/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value(this.bookDto.getIsbn()))
                .andExpect(jsonPath("$.title").value(this.bookDto.getTitle()));
    }
    
    @Test
    @Order(10)
    @WithMockUser
    void getBookByIsbn_withNonExistingIsbn_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/book/{isbn}",
                             "non-existing-isbn"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(11)
    @WithMockUser
    void doesBookExist_withExistingIsbn_shouldReturnTrue() throws Exception {
        this.mockMvc
                .perform(get("/api/book/exists/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(true));
    }
    
    @Test
    @Order(12)
    @WithMockUser
    void doesBookExist_withNonExistingIsbn_shouldReturnFalse() throws Exception {
        this.mockMvc
                .perform(get("/api/book/exists/{isbn}",
                             "non-existing-isbn"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
    
    @Test
    @Order(13)
    @WithMockUser
    void searchBookByKey_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(post("/api/book/search")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoFilter))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].isbn").value(this.bookDto.getIsbn()));
    }
    
    @Test
    @Order(14)
    @WithMockUser
    void searchBookByKey_withNonMatchingCriteria_shouldReturnEmpty() throws Exception {
        BookDto nonMatchingFilter = new BookDto("non-existing-isbn",
                                                "",
                                                "",
                                                "",
                                                "",
                                                new HashSet<>());
        
        this.mockMvc
                .perform(post("/api/book/search")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(nonMatchingFilter))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));
    }
    
    @Test
    @Order(15)
    @WithMockUser(roles = {"USER"})
    void updateBook_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             this.bookDto.getIsbn())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoNew))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(16)
    @WithMockUser(roles = {"MODERATOR"})
    void updateBook_withModeratorRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             this.bookDto.getIsbn())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoNew))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value(this.bookDtoNew.getTitle()))
                .andExpect(jsonPath("$.isbn").value(this.bookDto.getIsbn()));
    }
    
    @Test
    @Order(17)
    @WithMockUser(roles = {"MODERATOR"})
    void updateBook_withNonExistingIsbn_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             "non-existing-isbn")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.bookDtoNew))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(18)
    @WithMockUser(roles = {"MODERATOR"})
    void updateBook_withInvalidCategories_shouldReturnBadRequest() throws Exception {
        BookDto bookDtoWithInvalidCategories = new BookDto(this.bookDto.getIsbn(),
                                                           "title",
                                                           "description",
                                                           "author",
                                                           "url",
                                                           new HashSet<>(List.of(999L))
                                                           // Non-existing category
        );
        
        this.mockMvc
                .perform(put("/api/book/update/{isbn}",
                             this.bookDto.getIsbn())
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(bookDtoWithInvalidCategories))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(19)
    @WithMockUser(roles = {"MODERATOR"})
    void deleteBook_withModeratorRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(delete("/api/book/delete/{isbn}",
                                this.bookDto.getIsbn()).with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(20)
    @WithMockUser(roles = {"USER"})
    void deleteBook_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(delete("/api/book/delete/{isbn}",
                                this.bookDto.getIsbn()).with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(21)
    @WithMockUser(roles = {"ADMIN"})
    void deleteBook_withAdminRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(delete("/api/book/delete/{isbn}",
                                this.bookDto.getIsbn()).with(csrf()))
                .andExpect(status().isNoContent());
        
        // Verify the book is deleted
        this.mockMvc
                .perform(get("/api/book/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isNotFound());
        
        // Verify exists returns false
        this.mockMvc
                .perform(get("/api/book/exists/{isbn}",
                             this.bookDto.getIsbn()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(false));
    }
    
    @Test
    @Order(22)
    @WithMockUser(roles = {"ADMIN"})
    void deleteBook_withNonExistingIsbn_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(delete("/api/book/delete/{isbn}",
                                "non-existing-isbn").with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(23)
    @WithMockUser(roles = {"MODERATOR"})
    void createBook_withInvalidData_shouldReturnBadRequest() throws Exception {
        BookDto invalidBookDto = new BookDto("",
                                             "",
                                             "",
                                             "",
                                             "",
                                             new HashSet<>());
        
        this.mockMvc
                .perform(post("/api/book/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(invalidBookDto))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(24)
    @WithMockUser(roles = {"ADMIN"})
    void cleanupCategories() throws Exception {
        // Clean up categories
        this.mockMvc
                .perform(delete("/api/category/delete/1").with(csrf()))
                .andExpect(status().isOk());
        
        this.mockMvc
                .perform(delete("/api/category/delete/2").with(csrf()))
                .andExpect(status().isOk());
    }
}