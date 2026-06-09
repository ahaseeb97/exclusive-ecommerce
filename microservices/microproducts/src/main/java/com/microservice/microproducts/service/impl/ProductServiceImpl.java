/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microproducts.service.impl;

import com.microservice.microproducts.service.ProductService;
import com.microservice.Helper.s3.service.S3Service;
import com.microservice.microproducts.repository.ProductsRepository;
import com.microservice.microproducts.repository.CategoryRepository;
import com.microservice.microproducts.entity.Products;
import com.microservice.microproducts.entity.Category;
import com.microservice.microproducts.dto.ProductDTO;
import com.microservice.microproducts.dto.CategoryDTO;
import com.microservice.microproducts.dto.ProductFilter;
import com.microservice.microproducts.specification.ProductSpecifications;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;
import java.util.ArrayList;
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
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductsRepository productsRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private S3Service s3Service;

    @Override
    public List<ProductDTO> getAllProducts() {

        List<Products> products = productsRepository.findAll();
        return products.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO getProductById(Long id) {

        Products product = productsRepository.findById(id).orElse(null);
        if (product == null) {
            return null;
        }
        return convertToDTO(product);
    }

    @Override
    public ProductDTO createProduct(Products product) {

        try {

            Products newproduct = new Products();
            newproduct.setName(product.getName());
            newproduct.setPrice(product.getPrice());
            newproduct.setQuantity(product.getQuantity());
            newproduct.setImagePath(product.getImagePath());
            
            // Set all new fields
            newproduct.setDescription(product.getDescription());
            newproduct.setShortDescription(product.getShortDescription());
            // Handle Category - if product has a category, fetch it from repository
            if (product.getCategory() != null && product.getCategory().getId() != null) {
                Category category = categoryRepository.findById(product.getCategory().getId()).orElse(null);
                if (category != null) {
                    newproduct.setCategory(category);
                }
            }
            newproduct.setBrand(product.getBrand());
            newproduct.setSku(product.getSku());
            newproduct.setOriginalPrice(product.getOriginalPrice());
            newproduct.setDiscountPercentage(product.getDiscountPercentage());
            
            // Handle image paths
            if (product.getImagePaths() != null && !product.getImagePaths().isEmpty()) {
                newproduct.setImagePaths(new ArrayList<>(product.getImagePaths()));
            }
            
            newproduct.setAverageRating(product.getAverageRating());
            newproduct.setReviewCount(product.getReviewCount());
            
            if (product.getFeatures() != null) {
                newproduct.setFeatures(new ArrayList<>(product.getFeatures()));
            }
            if (product.getTags() != null) {
                newproduct.setTags(new ArrayList<>(product.getTags()));
            }
            if (product.getSpecifications() != null) {
                newproduct.setSpecifications(product.getSpecifications());
            }
            
            newproduct.setWeight(product.getWeight());
            newproduct.setWeightUnit(product.getWeightUnit());
            newproduct.setLength(product.getLength());
            newproduct.setWidth(product.getWidth());
            newproduct.setHeight(product.getHeight());
            newproduct.setDimensionUnit(product.getDimensionUnit());
            
            newproduct.setViewsCount(product.getViewsCount());
            newproduct.setIsActive(product.getIsActive() != null ? product.getIsActive() : true);
            newproduct.setIsFeatured(product.getIsFeatured() != null ? product.getIsFeatured() : false);
            
            newproduct.setMetaTitle(product.getMetaTitle());
            newproduct.setMetaDescription(product.getMetaDescription());
            newproduct.setSlug(product.getSlug());
            
            newproduct.setCreatedAt(new Date());
            newproduct.setUpdatedAt(new Date());

            productsRepository.save(newproduct);

            return convertToDTO(newproduct);
        } catch (Exception e) {
            throw new RuntimeException("New product creation failed: " + e.getMessage());
        }

    }
    
    @Override
    public ProductDTO updateProduct(Long id, Products product) {

        try {
            
            Products updateproduct = productsRepository.findById(id).orElse(null);
            
            if(updateproduct == null){
                throw new RuntimeException("Product not found");
            }
            
            // Update the existing product's fields
            updateproduct.setName(product.getName());
            updateproduct.setPrice(product.getPrice());
            updateproduct.setQuantity(product.getQuantity());
            updateproduct.setImagePath(product.getImagePath());
            
            // Update all new fields
            if (product.getDescription() != null) {
                updateproduct.setDescription(product.getDescription());
            }
            if (product.getShortDescription() != null) {
                updateproduct.setShortDescription(product.getShortDescription());
            }
            // Handle Category - if product has a category, set it
            if (product.getCategory() != null) {
                updateproduct.setCategory(product.getCategory());
            }
            if (product.getBrand() != null) {
                updateproduct.setBrand(product.getBrand());
            }
            if (product.getSku() != null) {
                updateproduct.setSku(product.getSku());
            }
            if (product.getOriginalPrice() != null) {
                updateproduct.setOriginalPrice(product.getOriginalPrice());
            }
            if (product.getDiscountPercentage() != null) {
                updateproduct.setDiscountPercentage(product.getDiscountPercentage());
            }
            
            if (product.getImagePaths() != null) {
                updateproduct.setImagePaths(new ArrayList<>(product.getImagePaths()));
            }
            
            if (product.getAverageRating() != null) {
                updateproduct.setAverageRating(product.getAverageRating());
            }
            if (product.getReviewCount() != null) {
                updateproduct.setReviewCount(product.getReviewCount());
            }
            
            if (product.getFeatures() != null) {
                updateproduct.setFeatures(new ArrayList<>(product.getFeatures()));
            }
            if (product.getTags() != null) {
                updateproduct.setTags(new ArrayList<>(product.getTags()));
            }
            if (product.getSpecifications() != null) {
                updateproduct.setSpecifications(product.getSpecifications());
            }
            
            if (product.getWeight() != null) {
                updateproduct.setWeight(product.getWeight());
            }
            if (product.getWeightUnit() != null) {
                updateproduct.setWeightUnit(product.getWeightUnit());
            }
            if (product.getLength() != null) {
                updateproduct.setLength(product.getLength());
            }
            if (product.getWidth() != null) {
                updateproduct.setWidth(product.getWidth());
            }
            if (product.getHeight() != null) {
                updateproduct.setHeight(product.getHeight());
            }
            if (product.getDimensionUnit() != null) {
                updateproduct.setDimensionUnit(product.getDimensionUnit());
            }
            
            if (product.getViewsCount() != null) {
                updateproduct.setViewsCount(product.getViewsCount());
            }
            if (product.getIsActive() != null) {
                updateproduct.setIsActive(product.getIsActive());
            }
            if (product.getIsFeatured() != null) {
                updateproduct.setIsFeatured(product.getIsFeatured());
            }
            
            if (product.getMetaTitle() != null) {
                updateproduct.setMetaTitle(product.getMetaTitle());
            }
            if (product.getMetaDescription() != null) {
                updateproduct.setMetaDescription(product.getMetaDescription());
            }
            if (product.getSlug() != null) {
                updateproduct.setSlug(product.getSlug());
            }
            
            // Always update the updatedAt timestamp
            updateproduct.setUpdatedAt(new Date());

            // Save the updated product (with existing ID)
            productsRepository.save(updateproduct);
            return convertToDTO(updateproduct);
            
        }
        catch (Exception e) {
            throw new RuntimeException("Product update failed: " + e.getMessage());
        }

    }

    @Override
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        try {
            if (categoryId == null) {
                // If no category ID provided, return all products
                return getAllProducts();
            }
            
            List<Products> products = productsRepository.findByCategoryId(categoryId);
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products by category: " + e.getMessage());
        }
    }
    
    @Override
    public ProductDTO stocksUpdate(Long id, Integer quantityChange) {
        try {
            Products product = productsRepository.findById(id).orElse(null);
            
            if (product == null) {
                throw new RuntimeException("Product not found");
            }
            
            // Get current quantity from database
            Integer currentQuantity = product.getQuantity() != null ? product.getQuantity() : 0;
            
            // Calculate new quantity (add quantityChange: positive increases, negative decreases)
            Integer newQuantity = currentQuantity + quantityChange;
            
            // Ensure quantity doesn't go below zero when decreasing
            if (newQuantity < 0) {
                throw new RuntimeException("Insufficient quantity. Current: " + currentQuantity + 
                    ", Requested change: " + quantityChange + ", Result would be: " + newQuantity);
            }
            
            // Update quantity
            product.setQuantity(newQuantity);
            product.setUpdatedAt(new Date());
            
            productsRepository.save(product);
            return convertToDTO(product);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update product quantity: " + e.getMessage());
        }
    }
    
    @Override
    public List<ProductDTO> getProductsWithFilters(ProductFilter filter) {
        try {
            Specification<Products> spec = ProductSpecifications.withFilters(filter);
            
            // Build Sort object if sort parameters are provided
            Sort sort = buildSort(filter);
            
            List<Products> products;
            if (sort != null) {
                products = productsRepository.findAll(spec, sort);
            } else {
                products = productsRepository.findAll(spec);
            }
            
            return products.stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch products with filters: " + e.getMessage());
        }
    }
    
    /**
     * Builds a Sort object from the filter's sort parameters.
     * Valid sort fields: name, price, averageRating, createdAt, updatedAt, quantity
     * Valid sort directions: asc, desc (defaults to asc if invalid)
     * 
     * @param filter ProductFilter containing sort parameters
     * @return Sort object or null if no valid sort parameters
     */
    private Sort buildSort(ProductFilter filter) {
        if (filter.getSortBy() == null || filter.getSortBy().trim().isEmpty()) {
            return null;
        }
        
        String sortBy = filter.getSortBy().trim().toLowerCase();
        String sortDirection = filter.getSortDirection() != null 
            ? filter.getSortDirection().trim().toLowerCase() 
            : "asc";
        
        // Validate and map sort field
        String fieldName = null;
        switch (sortBy) {
            case "name":
                fieldName = "name";
                break;
            case "price":
                fieldName = "price";
                break;
            case "rating":
            case "averagerating":
                fieldName = "averageRating";
                break;
            case "created":
            case "createdat":
                fieldName = "createdAt";
                break;
            case "updated":
            case "updatedat":
                fieldName = "updatedAt";
                break;
            case "quantity":
                fieldName = "quantity";
                break;
            default:
                // Invalid sort field, return null (no sorting)
                return null;
        }
        
        // Validate sort direction
        Sort.Direction direction = "desc".equals(sortDirection) 
            ? Sort.Direction.DESC 
            : Sort.Direction.ASC;
        
        return Sort.by(direction, fieldName);
    }
    
    /**
     * Converts Products entity to ProductDTO with presigned URLs
     */
    private ProductDTO convertToDTO(Products product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setPrice(product.getPrice());
        dto.setQuantity(product.getQuantity());
        
        // Generate presigned URL for primary image
        String primaryImageUrl = null;
        if (product.getImagePath() != null && !product.getImagePath().isEmpty()) {
            primaryImageUrl = s3Service.generatePresignedUrl(product.getImagePath());
            dto.setImageUrl(primaryImageUrl);
        }
        
        // Generate presigned URLs for multiple images
        List<String> allImageUrls = new ArrayList<>();
        
        // If imagePath exists, add it as the first image
        if (primaryImageUrl != null) {
            allImageUrls.add(primaryImageUrl);
        }
        
        // Add images from imagePaths array (if they exist)
        if (product.getImagePaths() != null && !product.getImagePaths().isEmpty()) {
            List<String> additionalUrls = product.getImagePaths().stream()
                    .map(imagePath -> s3Service.generatePresignedUrl(imagePath))
                    .collect(Collectors.toList());
            allImageUrls.addAll(additionalUrls);
        }
        
        // Set the combined list (will be empty if neither exists)
        if (!allImageUrls.isEmpty()) {
            dto.setImageUrls(allImageUrls);
        }
        
        // Product Information
        dto.setDescription(product.getDescription());
        dto.setShortDescription(product.getShortDescription());
        // Convert Category entity to CategoryDTO
        if (product.getCategory() != null) {
            dto.setCategory(convertCategoryToDTO(product.getCategory()));
        }
        dto.setBrand(product.getBrand());
        dto.setSku(product.getSku());
        
        // Pricing
        dto.setOriginalPrice(product.getOriginalPrice());
        dto.setDiscountPercentage(product.getDiscountPercentage());
        
        // Stock Status - Calculate from quantity
        boolean inStock = product.getQuantity() != null && product.getQuantity() > 0;
        dto.setInStock(inStock);
        
        // Determine availability status
        if (product.getQuantity() == null || product.getQuantity() == 0) {
            dto.setAvailability("Out of Stock");
        } else if (product.getQuantity() < 10) {
            dto.setAvailability("Low Stock");
        } else {
            dto.setAvailability("In Stock");
        }
        
        // Ratings and Reviews
        dto.setAverageRating(product.getAverageRating());
        dto.setReviewCount(product.getReviewCount());
        
        // Product Details
        dto.setFeatures(product.getFeatures());
        dto.setTags(product.getTags());
        dto.setSpecifications(product.getSpecifications());
        
        // Dimensions and Weight
        dto.setWeight(product.getWeight());
        dto.setWeightUnit(product.getWeightUnit());
        dto.setLength(product.getLength());
        dto.setWidth(product.getWidth());
        dto.setHeight(product.getHeight());
        dto.setDimensionUnit(product.getDimensionUnit());
        
        // Metadata
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        dto.setViewsCount(product.getViewsCount());
        dto.setIsActive(product.getIsActive());
        dto.setIsFeatured(product.getIsFeatured());
        
        // SEO
        dto.setMetaTitle(product.getMetaTitle());
        dto.setMetaDescription(product.getMetaDescription());
        dto.setSlug(product.getSlug());
        
        return dto;
    }
    
    /**
     * Converts Category entity to CategoryDTO
     */
    private CategoryDTO convertCategoryToDTO(Category category) {
        if (category == null) {
            return null;
        }
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
