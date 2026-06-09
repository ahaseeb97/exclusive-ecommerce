/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microservice.microproducts.service;

import com.microservice.microproducts.dto.CategoryDTO;
import com.microservice.microproducts.entity.Category;
import java.util.List;

/**
 *
 * @author abdul.haseeb
 */
public interface CategoryService {
    
    List<CategoryDTO> getAllCategories();
    
    CategoryDTO getCategoryById(Long id);
    
    CategoryDTO createCategory(Category category);
    
    CategoryDTO updateCategory(Long id, Category category);
    
    void deleteCategory(Long id);
    
}

