/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microproducts.dto;

import java.util.List;

/**
 * Filter criteria for product queries.
 * All fields are optional - only non-null fields will be applied as filters.
 * 
 * @author abdul.haseeb
 */
public class ProductFilter {
    private String search;              // Search keyword (searches name, description, brand, tags)
    private Long categoryId;            // Filter by category
    private String brand;               // Filter by specific brand
    private Double minPrice;            // Minimum price filter
    private Double maxPrice;            // Maximum price filter
    private Boolean inStock;            // Filter by stock availability
    private Boolean isActive;           // Filter by active status
    private Boolean isFeatured;         // Filter featured products
    private Double minRating;           // Minimum average rating
    private List<String> tags;          // Filter by tags (any of the provided tags)
    
    // Sorting fields
    private String sortBy;              // Field to sort by (name, price, averageRating, createdAt, updatedAt, quantity)
    private String sortDirection;       // Sort direction (asc, desc)
    
    // Getters and Setters
    public String getSearch() { 
        return search; 
    }
    
    public void setSearch(String search) { 
        this.search = search; 
    }
    
    public Long getCategoryId() { 
        return categoryId; 
    }
    
    public void setCategoryId(Long categoryId) { 
        this.categoryId = categoryId; 
    }
    
    public String getBrand() { 
        return brand; 
    }
    
    public void setBrand(String brand) { 
        this.brand = brand; 
    }
    
    public Double getMinPrice() { 
        return minPrice; 
    }
    
    public void setMinPrice(Double minPrice) { 
        this.minPrice = minPrice; 
    }
    
    public Double getMaxPrice() { 
        return maxPrice; 
    }
    
    public void setMaxPrice(Double maxPrice) { 
        this.maxPrice = maxPrice; 
    }
    
    public Boolean getInStock() { 
        return inStock; 
    }
    
    public void setInStock(Boolean inStock) { 
        this.inStock = inStock; 
    }
    
    public Boolean getIsActive() { 
        return isActive; 
    }
    
    public void setIsActive(Boolean isActive) { 
        this.isActive = isActive; 
    }
    
    public Boolean getIsFeatured() { 
        return isFeatured; 
    }
    
    public void setIsFeatured(Boolean isFeatured) { 
        this.isFeatured = isFeatured; 
    }
    
    public Double getMinRating() { 
        return minRating; 
    }
    
    public void setMinRating(Double minRating) { 
        this.minRating = minRating; 
    }
    
    public List<String> getTags() { 
        return tags; 
    }
    
    public void setTags(List<String> tags) { 
        this.tags = tags; 
    }
    
    public String getSortBy() { 
        return sortBy; 
    }
    
    public void setSortBy(String sortBy) { 
        this.sortBy = sortBy; 
    }
    
    public String getSortDirection() { 
        return sortDirection; 
    }
    
    public void setSortDirection(String sortDirection) { 
        this.sortDirection = sortDirection; 
    }
}

