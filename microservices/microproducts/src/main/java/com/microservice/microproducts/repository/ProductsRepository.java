/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microservice.microproducts.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.microservice.microproducts.entity.Products;
import com.microservice.microproducts.entity.Category;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 *
 * @author abdul.haseeb
 */
@Repository
public interface ProductsRepository extends JpaRepository<Products, Long>, JpaSpecificationExecutor<Products> {
    
//    List<Products> findAll();
    
    List<Products> findByCategory(Category category);
    
    List<Products> findByCategoryId(Long categoryId);
    
    /**
     * Searches products by keyword across multiple fields: name, description, shortDescription, brand, and tags.
     * Case-insensitive search using LIKE with wildcards.
     * Kept for backward compatibility.
     * 
     * @param keyword Search keyword
     * @return List of products matching the search criteria
     */
    @Query("SELECT p FROM Products p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.shortDescription) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.brand) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "EXISTS (SELECT t FROM p.tags t WHERE LOWER(t) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Products> searchProductsByKeyword(@Param("keyword") String keyword);
    
}
