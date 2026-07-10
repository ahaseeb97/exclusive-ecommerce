/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microorders.microorders.feignClients;

import com.microorders.microorders.feignClients.DTO.Products;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 * @author abdul.haseeb
 */
@FeignClient(name = "MICROPRODUCTS")
public interface ProductsClient {
    
    @GetMapping("/api/products/all")
    List<Products> getAllProducts();
    
    @GetMapping("/api/products/{id}")
    Products getProductById(@PathVariable Long id);
    
    @PutMapping("/api/products/stocks-update/{id}")
    Products stocksUpdate(@PathVariable Long id, @RequestParam Integer quantityChange);
    
}
