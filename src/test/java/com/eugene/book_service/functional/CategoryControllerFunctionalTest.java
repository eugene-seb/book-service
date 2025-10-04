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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    void createCategory_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(2)
    @WithMockUser(roles = {"USER"})
    void createCategory_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(3)
    @WithMockUser(roles = {"MODERATOR"})
    void createCategory_withModeratorRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/category/1"))
                .andExpect(jsonPath("$.name").value(this.categoryDto.getName()))
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    @Order(4)
    @WithMockUser(roles = {"MODERATOR"})
    void createCategory_withDuplicateName_shouldReturnConflict() throws Exception {
        // Try to create the same category again
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(5)
    @WithMockUser(roles = {"MODERATOR"})
    void createCategory_withBlankName_shouldReturnBadRequest() throws Exception {
        CategoryDto blankCategory = new CategoryDto("");
        
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(blankCategory))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(6)
    @WithMockUser(roles = {"MODERATOR"})
    void createCategory_withNullName_shouldReturnBadRequest() throws Exception {
        CategoryDto nullCategory = new CategoryDto(null);
        
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(nullCategory))
                                 .with(csrf()))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @Order(7)
    void getAllCategories_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/category/all"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(8)
    @WithMockUser
    void getAllCategories_withAuthenticatedUser_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value(this.categoryDto.getName()));
    }
    
    @Test
    @Order(9)
    void getCategoryById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/category/1"))
                .andExpect(status().isUnauthorized());
    }
    
    @Test
    @Order(10)
    @WithMockUser
    void getCategoryById_withValidId_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(get("/api/category/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value(this.categoryDto.getName()));
    }
    
    @Test
    @Order(11)
    @WithMockUser
    void getCategoryById_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(get("/api/category/999"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(12)
    @WithMockUser(roles = {"USER"})
    void updateCategory_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(put("/api/category/update/1")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDtoNew))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(13)
    @WithMockUser(roles = {"MODERATOR"})
    void updateCategory_withModeratorRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(put("/api/category/update/1")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDtoNew))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(this.categoryDtoNew.getName()))
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    @Order(14)
    @WithMockUser(roles = {"MODERATOR"})
    void updateCategory_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(put("/api/category/update/999")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDtoNew))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(15)
    @WithMockUser(roles = {"MODERATOR"})
    void updateCategory_withDuplicateName_shouldReturnConflict() throws Exception {
        // First create another category
        CategoryDto anotherCategory = new CategoryDto("science");
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(anotherCategory))
                                 .with(csrf()))
                .andExpect(status().isCreated());
        
        // Try to update the second category with the same name as the first
        this.mockMvc
                .perform(put("/api/category/update/2")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDtoNew)) // "music" which already exists from update
                                 .with(csrf()))
                .andExpect(status().isConflict());
    }
    
    @Test
    @Order(16)
    @WithMockUser(roles = {"MODERATOR"})
    void deleteCategory_withModeratorRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(delete("/api/category/delete/2").with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(17)
    @WithMockUser(roles = {"USER"})
    void deleteCategory_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(delete("/api/category/delete/2").with(csrf()))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @Order(18)
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_withAdminRole_shouldSucceed() throws Exception {
        this.mockMvc
                .perform(delete("/api/category/delete/2").with(csrf()))
                .andExpect(status().isOk());
        
        // Verify the category is deleted
        this.mockMvc
                .perform(get("/api/category/2"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(19)
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_withNonExistingId_shouldReturnNotFound() throws Exception {
        this.mockMvc
                .perform(delete("/api/category/delete/999").with(csrf()))
                .andExpect(status().isNotFound());
    }
    
    @Test
    @Order(20)
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_finalCleanup() throws Exception {
        // Clean up remaining categories
        this.mockMvc
                .perform(delete("/api/category/delete/1").with(csrf()))
                .andExpect(status().isOk());
    }
}