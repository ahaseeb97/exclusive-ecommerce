/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microproducts.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.microservice.microproducts.service.ProductService;
import com.microservice.microproducts.service.CategoryService;
import com.microservice.microproducts.entity.Products;
import com.microservice.microproducts.dto.ProductDTO;
import com.microservice.microproducts.dto.CategoryDTO;
import com.microservice.microproducts.dto.ProductFilter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.microservice.Helper.s3.service.S3Service;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author abdul.haseeb
 */
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private S3Service s3Service;

    /**
     * Gets all products with optional filtering.
     * Supports multiple filter parameters that can be combined.
     * 
     * @param search Search keyword (searches name, description, brand, tags)
     * @param categoryId Filter by category ID
     * @param brand Filter by specific brand
     * @param minPrice Minimum price filter
     * @param maxPrice Maximum price filter
     * @param inStock Filter by stock availability (true = in stock, false = out of stock)
     * @param isActive Filter by active status
     * @param isFeatured Filter featured products
     * @param minRating Minimum average rating
     * @param tags Filter by tags (products must have at least one of the specified tags)
     * @param sortBy Field to sort by (name, price, rating, createdAt, updatedAt, quantity)
     * @param sortDirection Sort direction (asc, desc) - defaults to asc
     * @return List of products matching the filter criteria, sorted if sort parameters provided
     */
    @GetMapping("/all")
    public ResponseEntity<List<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortDirection) {
        try {
            // Build filter object from request parameters
            ProductFilter filter = new ProductFilter();
            filter.setSearch(search);
            filter.setCategoryId(categoryId);
            filter.setBrand(brand);
            filter.setMinPrice(minPrice);
            filter.setMaxPrice(maxPrice);
            filter.setInStock(inStock);
            filter.setIsActive(isActive);
            filter.setIsFeatured(isFeatured);
            filter.setMinRating(minRating);
            filter.setTags(tags);
            filter.setSortBy(sortBy);
            filter.setSortDirection(sortDirection);
            
            List<ProductDTO> products = productService.getProductsWithFilters(filter);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ArrayList<>());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {

        ProductDTO product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(product);
    }
    
    /**
     * Gets all categories for product creation/editing
     * 
     * @return List of all categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        try {
            List<CategoryDTO> categories = categoryService.getAllCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<?> createProduct(@RequestBody Products products) {

        try {
            ProductDTO product = productService.createProduct(products);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateProduct(@PathVariable Long id, @RequestBody Products products) {

        try {
            ProductDTO product = productService.updateProduct(id, products);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Updates product quantity by adding or removing a specified amount.
     * Positive quantityChange increases quantity, negative quantityChange decreases quantity.
     * Current quantity is retrieved from the database automatically.
     * 
     * @param id Product ID
     * @param quantityChange The quantity change (positive to increase, negative to decrease)
     * @return Updated product DTO
     */
    @PutMapping("/stocks-update/{id}")
    public ResponseEntity<?> stocksUpdate(
            @PathVariable Long id,
            @RequestParam Integer quantityChange) {
        try {
            ProductDTO product = productService.stocksUpdate(id, quantityChange);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Uploads one or more images to S3.
     * The first image is treated as the primary/main image.
     * Additional images are stored as additional image paths.
     * 
     * @param files Array of image files (can be single or multiple)
     * @return Response containing primaryImagePath and imagePaths list
     */
    @PostMapping("/upload-images")
    public ResponseEntity<?> uploadImages(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files == null || files.length == 0) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No files provided");
            }
            
            List<String> uploadedKeys = new ArrayList<>();
            List<String> uploadedFilenames = new ArrayList<>();
            
            for (MultipartFile file : files) {
                if (file.isEmpty()) {
                    continue;
                }
                
                String contentType = file.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    continue;
                }
                
                // Upload to S3 (returns full path with prefix)
                String objectKey = s3Service.uploadFile(file.getInputStream(), file.getOriginalFilename(), contentType);
                uploadedKeys.add(objectKey);
                
                // Extract only the filename (without prefix) for database storage
                String filename = extractFilename(objectKey);
                uploadedFilenames.add(filename);
            }
            
            if (uploadedFilenames.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No valid images uploaded");
            }
            
            // First image is the primary image, rest are additional images
            // Return only filenames (without S3 prefix) for database storage
            String primaryImagePath = uploadedFilenames.get(0);
            List<String> additionalImagePaths = uploadedFilenames.size() > 1 
                    ? uploadedFilenames.subList(1, uploadedFilenames.size()) 
                    : new ArrayList<>();
            
            // Return response with primary image and additional image paths only (excluding primary)
            // imagePaths should only contain additional images, not the primary one
            Map<String, Object> response = new HashMap<>();
            response.put("primaryImagePath", primaryImagePath);
            response.put("imagePaths", additionalImagePaths); // Only additional images, excluding primary
            response.put("additionalImagePaths", additionalImagePaths);
            
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload images: " + e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Deletes one or more images from S3.
     * Used for cleanup when product creation fails.
     * 
     * @param imagePaths List of image paths (object keys) to delete
     * @return Response with list of successfully deleted image paths
     */
    @DeleteMapping("/delete-images")
    public ResponseEntity<?> deleteImages(@RequestBody List<String> imagePaths) {
        try {
            if (imagePaths == null || imagePaths.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("No image paths provided");
            }
            
            List<String> deletedPaths = s3Service.deleteFiles(imagePaths);
            
            Map<String, Object> response = new HashMap<>();
            response.put("deletedPaths", deletedPaths);
            response.put("totalRequested", imagePaths.size());
            response.put("totalDeleted", deletedPaths.size());
            response.put("failedCount", imagePaths.size() - deletedPaths.size());
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    
    /**
     * Extracts the filename from a full S3 object key/path.
     * Removes the prefix (e.g., "local/products/images/") and returns only the filename.
     * 
     * @param objectKey Full S3 object key (e.g., "local/products/images/filename.jpg")
     * @return Just the filename (e.g., "filename.jpg")
     */
    private String extractFilename(String objectKey) {
        if (objectKey == null || objectKey.trim().isEmpty()) {
            return objectKey;
        }
        
        // Find the last slash to get the filename
        int lastSlashIndex = objectKey.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < objectKey.length() - 1) {
            return objectKey.substring(lastSlashIndex + 1);
        }
        
        // If no slash found, return as is (already just filename)
        return objectKey;
    }

}
