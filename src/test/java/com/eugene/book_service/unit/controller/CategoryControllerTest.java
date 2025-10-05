package com.eugene.book_service.unit.controller;

import com.eugene.book_service.config.SecurityConfig;
import com.eugene.book_service.controller.CategoryController;
import com.eugene.book_service.dto.CategoryDto;
import com.eugene.book_service.exception.NotFoundException;
import com.eugene.book_service.model.Category;
import com.eugene.book_service.service.CategoryService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CategoryControllerTest
{
    private CategoryDto categoryDto;
    private Category category;
    private List<Category> categoryList;
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private CategoryService categoryService;
    
    static String asJsonString(final Object obj) {
        try {
            final ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @BeforeEach
    void initVariables() {
        this.categoryDto = new CategoryDto("Fiction");
        
        this.category = new Category("Fiction");
        this.category.setId(1L);
        
        this.categoryList = List.of(new Category("Fiction"),
                                    new Category("Science"),
                                    new Category("History"));
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR"})
    void createCategory_withModeratorRole_shouldSucceed() throws Exception {
        given(this.categoryService.createCategory(any(CategoryDto.class))).willReturn(this.category);
        
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                                           "/api/category/" + this.category.getId()))
                .andExpect(jsonPath("$.id").value(this.category.getId()))
                .andExpect(jsonPath("$.name").value(this.category.getName()));
        
        verify(this.categoryService).createCategory(any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createCategory_withAdminRole_shouldSucceed() throws Exception {
        given(this.categoryService.createCategory(any(CategoryDto.class))).willReturn(this.category);
        
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isCreated());
        
        verify(this.categoryService).createCategory(any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void createCategory_withUserRole_shouldBeForbidden() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(this.categoryService,
               never()).createCategory(any(CategoryDto.class));
    }
    
    @Test
    void createCategory_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(post("/api/category/create")
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.categoryService,
               never()).createCategory(any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser
    void getAllCategories_withAuthenticatedUser_shouldSucceed() throws Exception {
        given(this.categoryService.getAllCategories()).willReturn(this.categoryList);
        
        this.mockMvc
                .perform(get("/api/category/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(this.categoryList.size()))
                .andExpect(jsonPath("$[1].name").value(this.categoryList
                                                               .get(1)
                                                               .getName()));
        
        verify(this.categoryService).getAllCategories();
    }
    
    @Test
    void getAllCategories_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/category/all"))
                .andExpect(status().isUnauthorized());
        
        verify(this.categoryService,
               never()).getAllCategories();
    }
    
    @Test
    @WithMockUser
    void getCategoryById_withValidId_shouldSucceed() throws Exception {
        Long categoryId = 1L;
        given(this.categoryService.getCategoryById(categoryId)).willReturn(this.category);
        
        this.mockMvc
                .perform(get("/api/category/{idCategory}",
                             categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.category.getId()))
                .andExpect(jsonPath("$.name").value(this.category.getName()));
        
        verify(this.categoryService).getCategoryById(categoryId);
    }
    
    @Test
    @WithMockUser
    void getCategoryById_withNonExistingId_shouldReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        given(this.categoryService.getCategoryById(nonExistingId)).willThrow(new NotFoundException("Category not found",
                                                                                                   null));
        
        this.mockMvc
                .perform(get("/api/category/{idCategory}",
                             nonExistingId))
                .andExpect(status().isNotFound());
        
        verify(this.categoryService).getCategoryById(nonExistingId);
    }
    
    @Test
    void getCategoryById_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        this.mockMvc
                .perform(get("/api/category/{idCategory}",
                             1L))
                .andExpect(status().isUnauthorized());
        
        verify(this.categoryService,
               never()).getCategoryById(anyLong());
    }
    
    // UPDATE CATEGORY TESTS
    @Test
    @WithMockUser(roles = {"MODERATOR"})
    void updateCategory_withModeratorRole_shouldSucceed() throws Exception {
        Long categoryId = 1L;
        given(this.categoryService.updateCategory(eq(categoryId),
                                                  any(CategoryDto.class))).willReturn(this.category);
        
        this.mockMvc
                .perform(put("/api/category/update/{idCategory}",
                             categoryId)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.category.getId()))
                .andExpect(jsonPath("$.name").value(this.category.getName()));
        
        verify(this.categoryService).updateCategory(eq(categoryId),
                                                    any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateCategory_withAdminRole_shouldSucceed() throws Exception {
        Long categoryId = 1L;
        given(this.categoryService.updateCategory(eq(categoryId),
                                                  any(CategoryDto.class))).willReturn(this.category);
        
        this.mockMvc
                .perform(put("/api/category/update/{idCategory}",
                             categoryId)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isOk());
        
        verify(this.categoryService).updateCategory(eq(categoryId),
                                                    any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void updateCategory_withUserRole_shouldBeForbidden() throws Exception {
        Long categoryId = 1L;
        
        this.mockMvc
                .perform(put("/api/category/update/{idCategory}",
                             categoryId)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(this.categoryService,
               never()).updateCategory(anyLong(),
                                       any(CategoryDto.class));
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR"})
    void updateCategory_withNonExistingId_shouldReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        given(this.categoryService.updateCategory(eq(nonExistingId),
                                                  any(CategoryDto.class))).willThrow(new NotFoundException("Category not found",
                                                                                                           null));
        
        this.mockMvc
                .perform(put("/api/category/update/{idCategory}",
                             nonExistingId)
                                 .contentType(MediaType.APPLICATION_JSON)
                                 .content(asJsonString(this.categoryDto))
                                 .with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.categoryService).updateCategory(eq(nonExistingId),
                                                    any(CategoryDto.class));
    }
    
    // DELETE CATEGORY TESTS
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_withAdminRole_shouldSucceed() throws Exception {
        Long categoryId = 1L;
        doNothing()
                .when(this.categoryService)
                .deleteCategory(categoryId);
        
        this.mockMvc
                .perform(delete("/api/category/delete/{idCategory}",
                                categoryId).with(csrf()))
                .andExpect(status().isOk());
        
        verify(this.categoryService).deleteCategory(categoryId);
    }
    
    @Test
    @WithMockUser(roles = {"MODERATOR"})
    void deleteCategory_withModeratorRole_shouldBeForbidden() throws Exception {
        Long categoryId = 1L;
        
        this.mockMvc
                .perform(delete("/api/category/delete/{idCategory}",
                                categoryId).with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(this.categoryService,
               never()).deleteCategory(anyLong());
    }
    
    @Test
    @WithMockUser(roles = {"USER"})
    void deleteCategory_withUserRole_shouldBeForbidden() throws Exception {
        Long categoryId = 1L;
        
        this.mockMvc
                .perform(delete("/api/category/delete/{idCategory}",
                                categoryId).with(csrf()))
                .andExpect(status().isForbidden());
        
        verify(this.categoryService,
               never()).deleteCategory(anyLong());
    }
    
    @Test
    void deleteCategory_withoutAuthentication_shouldBeUnauthorized() throws Exception {
        Long categoryId = 1L;
        
        this.mockMvc
                .perform(delete("/api/category/delete/{idCategory}",
                                categoryId).with(csrf()))
                .andExpect(status().isUnauthorized());
        
        verify(this.categoryService,
               never()).deleteCategory(anyLong());
    }
    
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategory_withNonExistingId_shouldReturnNotFound() throws Exception {
        Long nonExistingId = 999L;
        doThrow(new NotFoundException("Category not found",
                                      null))
                .when(this.categoryService)
                .deleteCategory(nonExistingId);
        
        this.mockMvc
                .perform(delete("/api/category/delete/{idCategory}",
                                nonExistingId).with(csrf()))
                .andExpect(status().isNotFound());
        
        verify(this.categoryService).deleteCategory(nonExistingId);
    }
}