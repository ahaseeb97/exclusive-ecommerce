/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microproducts.service.impl;

import com.microservice.microproducts.service.CategoryService;
import com.microservice.microproducts.repository.CategoryRepository;
import com.microservice.microproducts.entity.Category;
import com.microservice.microproducts.dto.CategoryDTO;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author abdul.haseeb
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElse(null);
        if (category == null) {
            return null;
        }
        return convertToDTO(category);
    }

    @Override
    public CategoryDTO createCategory(Category category) {
        try {
            Category newCategory = new Category();
            newCategory.setName(category.getName());
            newCategory.setDescription(category.getDescription());
            newCategory.setSlug(category.getSlug());
            newCategory.setIsActive(category.getIsActive() != null ? category.getIsActive() : true);
            newCategory.setCreatedAt(new Date());
            newCategory.setUpdatedAt(new Date());

            categoryRepository.save(newCategory);
            return convertToDTO(newCategory);
        } catch (Exception e) {
            throw new RuntimeException("Category creation failed: " + e.getMessage());
        }
    }

    @Override
    public CategoryDTO updateCategory(Long id, Category category) {
        try {
            Category updateCategory = categoryRepository.findById(id).orElse(null);

            if (updateCategory == null) {
                throw new RuntimeException("Category not found");
            }

            if (category.getName() != null) {
                updateCategory.setName(category.getName());
            }
            if (category.getDescription() != null) {
                updateCategory.setDescription(category.getDescription());
            }
            if (category.getSlug() != null) {
                updateCategory.setSlug(category.getSlug());
            }
            if (category.getIsActive() != null) {
                updateCategory.setIsActive(category.getIsActive());
            }

            updateCategory.setUpdatedAt(new Date());

            categoryRepository.save(updateCategory);
            return convertToDTO(updateCategory);

        } catch (Exception e) {
            throw new RuntimeException("Category update failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteCategory(Long id) {
        try {
            Category category = categoryRepository.findById(id).orElse(null);
            if (category == null) {
                throw new RuntimeException("Category not found");
            }
            categoryRepository.delete(category);
        } catch (Exception e) {
            throw new RuntimeException("Category deletion failed: " + e.getMessage());
        }
    }

    /**
     * Converts Category entity to CategoryDTO
     */
    private CategoryDTO convertToDTO(Category category) {
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setSlug(category.getSlug());
        dto.setIsActive(category.getIsActive());
        dto.setCreatedAt(category.getCreatedAt());
        dto.setUpdatedAt(category.getUpdatedAt());
        return dto;
    }

}

