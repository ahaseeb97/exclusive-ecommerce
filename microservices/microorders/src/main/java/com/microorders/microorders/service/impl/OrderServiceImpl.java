/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.microorders.microorders.service.impl;

import com.microorders.microorders.DTO.CreateOrderRequest;
import com.microorders.microorders.DTO.OrderItemDTO;
import com.microorders.microorders.DTO.OrdersDTO;
import com.microorders.microorders.entity.OrderItem;
import com.microorders.microorders.entity.OrderStatus;
import com.microorders.microorders.entity.Orders;
import com.microorders.microorders.feignClients.DTO.Products;
import com.microorders.microorders.feignClients.ProductsClient;
import com.microorders.microorders.repository.OrderRepository;
import com.microorders.microorders.service.OrderService;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author abdul.haseeb
 */
@Service
public class OrderServiceImpl implements OrderService{
    
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductsClient productsClient;
    
    @Override
    public List<Products> getAllProducts(){
        
        List<Products> products = productsClient.getAllProducts();
        return products;
    }
    
    @Override
    public List<OrdersDTO> getAllOrders(){
        
        List<Orders> orders = orderRepository.findAll();
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<OrdersDTO> getAllOrdersByUserId(Long userId){
        
        List<Orders> orders = orderRepository.findByUserId(userId);
        return orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public OrdersDTO createOrder(CreateOrderRequest request, Long userId) {
        Orders order = new Orders();
        order.setUserId(userId);
        order.setStatus(OrderStatus.ORDER_CONFIRMED); // Set default status
        double totalPrice = 0.0;
        
        List<OrderItem> orderItems = new ArrayList<>();
        
        for (CreateOrderRequest.OrderItemRequest itemRequest : request.getItems()) {
            Products product = productsClient.getProductById(itemRequest.getProductId());
            
            if (product == null) {
                throw new RuntimeException("Product with ID " + itemRequest.getProductId() + " not found");
            }
            
            double unitPrice = product.getPrice();
            int quantity = itemRequest.getQuantity();
            double subtotal = Math.round(unitPrice * quantity * 100.0) / 100.0;
            
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setSubtotal(subtotal);
            orderItem.setOrder(order);
            
            orderItems.add(orderItem);
            totalPrice += subtotal;
        }
        
        order.setTotalPrice(Math.round(totalPrice * 100.0) / 100.0);
        order.setOrderItems(orderItems);
        
        Orders savedOrder = orderRepository.save(order);
        
        // Reduce product quantities after order is successfully created
        for (OrderItem orderItem : savedOrder.getOrderItems()) {
            try {
                // Reduce quantity by the order item quantity (negative value)
                productsClient.stocksUpdate(orderItem.getProductId(), -orderItem.getQuantity());
            } catch (Exception e) {
                throw new RuntimeException("Failed to reduce quantity for product ID " + 
                    orderItem.getProductId() + ": " + e.getMessage());
            }
        }
        
        return convertToDTO(savedOrder);
    }
    
    @Override
    public OrdersDTO getOrderById(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found"));
        
        return convertToDTO(order);
    }
    
    @Override
    @Transactional
    public OrdersDTO updateOrderStatus(Long orderId, OrderStatus status) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found"));
        
        order.setStatus(status);
        Orders updatedOrder = orderRepository.save(order);
        
        return convertToDTO(updatedOrder);
    }
    
    @Override
    @Transactional
    public OrdersDTO cancelOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order with ID " + orderId + " not found"));
        
        // Check if order is already cancelled
        if (order.getStatus() == OrderStatus.ORDER_CANCELLED) {
            throw new RuntimeException("Order with ID " + orderId + " is already cancelled");
        }
        
        // Restore product quantities for all order items
        for (OrderItem orderItem : order.getOrderItems()) {
            try {
                // Restore quantity by adding back the order item quantity (positive value)
                productsClient.stocksUpdate(orderItem.getProductId(), orderItem.getQuantity());
            } catch (Exception e) {
                throw new RuntimeException("Failed to restore quantity for product ID " + 
                    orderItem.getProductId() + ": " + e.getMessage());
            }
        }
        
        // Update order status to cancelled
        order.setStatus(OrderStatus.ORDER_CANCELLED);
        Orders updatedOrder = orderRepository.save(order);
        
        return convertToDTO(updatedOrder);
    }
    
    private OrdersDTO convertToDTO(Orders order) {
        OrdersDTO dto = new OrdersDTO();
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(orderItem -> {
                    Products product = productsClient.getProductById(orderItem.getProductId());
                    OrderItemDTO itemDTO = new OrderItemDTO();
                    itemDTO.setOrderItemId(orderItem.getOrderItemId());
                    itemDTO.setProductId(orderItem.getProductId());
                    itemDTO.setProductName(product != null ? product.getName() : "Unknown Product");
                    // Use the presigned URL directly from the product service
                    String presignedUrl = product != null ? product.getImageUrl() : null;
                    itemDTO.setProductImageUrl(presignedUrl);
                    itemDTO.setQuantity(orderItem.getQuantity());
                    itemDTO.setUnitPrice(orderItem.getUnitPrice());
                    itemDTO.setSubtotal(orderItem.getSubtotal());
                    return itemDTO;
                })
                .collect(Collectors.toList());
        
        dto.setOrderItems(orderItemDTOs);
        return dto;
    }
    
}
