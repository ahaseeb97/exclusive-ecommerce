/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microservice.microproducts.specification;

import com.microservice.microproducts.entity.Products;
import com.microservice.microproducts.dto.ProductFilter;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for building dynamic JPA Specifications for product filtering.
 * 
 * @author abdul.haseeb
 */
public class ProductSpecifications {
    
    /**
     * Builds a dynamic specification based on the provided filter.
     * Each non-null filter field adds a condition to the query.
     * 
     * @param filter ProductFilter containing filter criteria
     * @return Specification<Products> for querying products
     */
    public static Specification<Products> withFilters(ProductFilter filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            boolean needsDistinct = false;
            
            // Search keyword filter (searches across multiple fields)
            if (filter.getSearch() != null && !filter.getSearch().trim().isEmpty()) {
                String searchPattern = "%" + filter.getSearch().trim().toLowerCase() + "%";
                Predicate namePredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")), searchPattern);
                Predicate descPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("description")), searchPattern);
                Predicate shortDescPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("shortDescription")), searchPattern);
                Predicate brandPredicate = criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("brand")), searchPattern);
                
                // Search in tags using EXISTS subquery to avoid join conflicts
                Subquery<Long> tagSubquery = query.subquery(Long.class);
                Root<Products> tagRoot = tagSubquery.correlate(root);
                Join<Products, String> tagJoin = tagRoot.join("tags", JoinType.LEFT);
                tagSubquery.select(criteriaBuilder.literal(1L))
                    .where(criteriaBuilder.like(
                        criteriaBuilder.lower(tagJoin), searchPattern));
                Predicate tagPredicate = criteriaBuilder.exists(tagSubquery);
                
                predicates.add(criteriaBuilder.or(
                    namePredicate, descPredicate, shortDescPredicate, 
                    brandPredicate, tagPredicate
                ));
            }
            
            // Category filter
            if (filter.getCategoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("category").get("id"), filter.getCategoryId()));
            }
            
            // Brand filter
            if (filter.getBrand() != null && !filter.getBrand().trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("brand")), 
                    filter.getBrand().trim().toLowerCase()));
            }
            
            // Price range filters
            if (filter.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("price"), filter.getMinPrice()));
            }
            if (filter.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("price"), filter.getMaxPrice()));
            }
            
            // Stock availability filter
            if (filter.getInStock() != null) {
                if (filter.getInStock()) {
                    predicates.add(criteriaBuilder.greaterThan(root.get("quantity"), 0));
                } else {
                    predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("quantity"), 0));
                }
            }
            
            // Active status filter
            if (filter.getIsActive() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isActive"), filter.getIsActive()));
            }
            
            // Featured filter
            if (filter.getIsFeatured() != null) {
                predicates.add(criteriaBuilder.equal(
                    root.get("isFeatured"), filter.getIsFeatured()));
            }
            
            // Minimum rating filter
            if (filter.getMinRating() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("averageRating"), filter.getMinRating()));
            }
            
            // Tags filter (products must have at least one of the specified tags)
            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                Join<Products, String> tagsJoin = root.join("tags", JoinType.LEFT);
                predicates.add(tagsJoin.in(filter.getTags()));
                needsDistinct = true; // Need distinct when joining with tags
            }
            
            // Set distinct if needed (for tag joins)
            if (needsDistinct) {
                query.distinct(true);
            }
            
            // Combine all predicates with AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}

