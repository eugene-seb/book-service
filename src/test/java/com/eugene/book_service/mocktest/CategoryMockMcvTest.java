package com.eugene.book_service.mocktest;

import com.eugene.book_service.dto.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CategoryMockMcvTest {

    private final CategoryDto categoryDto;
    private final CategoryDto categoryDtoNew;

    @Autowired
    private MockMvc mockMvc;

    public CategoryMockMcvTest() {
        this.categoryDto = new CategoryDto("art");
        this.categoryDtoNew = new CategoryDto("music");
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
    void createCategory() throws Exception {
        mockMvc
                .perform(post("/category/create_category")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/category?idCategory=1"))
                .andExpect(jsonPath("$.name").value("art"));
    }

    @Test
    @Order(2)
    void getAllCategory() throws Exception {

        mockMvc
                .perform(get("/category/all_categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    @Test
    @Order(3)
    void getCategoryById() throws Exception {

        mockMvc
                .perform(get("/category?idCategory=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"));
    }

    @Test
    @Order(4)
    void updateCategory() throws Exception {

        mockMvc
                .perform(put("/category/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(categoryDtoNew)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(categoryDtoNew.name()));
    }

    @Test
    @Order(5)
    void deleteCategory() throws Exception {

        mockMvc
                .perform(delete("/category/delete/1"))
                .andExpect(status().isOk());

        mockMvc
                .perform(get("/category?idCategory=1"))
                .andExpect(status().isNotFound());
    }
}
