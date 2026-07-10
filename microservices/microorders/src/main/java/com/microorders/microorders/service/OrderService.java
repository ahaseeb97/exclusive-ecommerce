/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microorders.microorders.service;

import com.microorders.microorders.DTO.CreateOrderRequest;
import com.microorders.microorders.DTO.OrdersDTO;
import com.microorders.microorders.entity.OrderStatus;
import com.microorders.microorders.feignClients.DTO.Products;
import java.util.List;

/**
 *
 * @author abdul.haseeb
 */
public interface OrderService {
    
    List<Products> getAllProducts();
    
    List<OrdersDTO> getAllOrders();
    
    List<OrdersDTO> getAllOrdersByUserId(Long userId);
    
    OrdersDTO createOrder(CreateOrderRequest request, Long userId);
    
    OrdersDTO getOrderById(Long orderId);
    
    OrdersDTO updateOrderStatus(Long orderId, OrderStatus status);
    
    OrdersDTO cancelOrder(Long orderId);
    
}
