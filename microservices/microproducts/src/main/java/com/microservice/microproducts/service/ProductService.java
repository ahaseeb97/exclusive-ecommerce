/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microservice.microproducts.service;

import com.microservice.microproducts.dto.ProductDTO;
import com.microservice.microproducts.dto.ProductFilter;
import com.microservice.microproducts.entity.Products;
import java.util.List;

/**
 *
 * @author abdul.haseeb
 */
public interface ProductService {
    
    List<ProductDTO> getAllProducts();
    
    ProductDTO getProductById(Long id);
    
    ProductDTO createProduct(Products product);
    
    ProductDTO updateProduct(Long id, Products product);
    
    List<ProductDTO> getProductsByCategory(Long categoryId);
    
    ProductDTO stocksUpdate(Long id, Integer quantityChange);
    
    /**
     * Gets products with dynamic filtering based on ProductFilter criteria.
     * This is the recommended method for querying products with multiple filters.
     * 
     * @param filter ProductFilter containing filter criteria
     * @return List of products matching the filter criteria
     */
    List<ProductDTO> getProductsWithFilters(ProductFilter filter);
    
}
