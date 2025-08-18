package com.eugene.book_service.functional;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportAutoConfiguration(exclude = {KafkaAutoConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class CategoryControllerFunctionalTest
{
    private final CategoryDto categoryDto;
    private final CategoryDto categoryDtoNew;
    
    /**
     * I don't want the context to load kafka for this test, so I'm mocking his initialization
     * It will replace all the KafkaTemplate instances.
     */
    @MockitoBean
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Autowired
    private MockMvc mockMvc;
    
    public CategoryControllerFunctionalTest() {
        this.categoryDto = new CategoryDto("art");
        this.categoryDtoNew = new CategoryDto("music");
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
    void createCategory() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/category/1"))
                .andExpect(jsonPath("$.name").value(this.categoryDto.getName()));
        
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto)))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(2)
    @WithMockUser
    void getAllCategory() throws Exception {
        
        this.mockMvc
                .perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }
    
    @Test
    @Order(3)
    @WithMockUser
    void getCategoryById() throws Exception {
        
        this.mockMvc
                .perform(get("/api/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }
    
    @Test
    @Order(4)
    @WithMockUser(roles = {"MODERATOR", "ADMIN"})
    void updateCategory() throws Exception {
        
        this.mockMvc
                .perform(put("/api/category/update/1")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDtoNew)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(this.categoryDtoNew.getName()));
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory() throws Exception {
        
        this.mockMvc
                .perform(delete("/api/category/delete/1"))
                .andExpect(status().isOk());
        
        this.mockMvc
                .perform(get("/api/category/1"))
                .andExpect(status().isNotFound());
    }
}
