/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microorders.microorders.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.microorders.microorders.entity.Orders;
import java.util.List;
import org.springframework.stereotype.Repository;

/**
 *
 * @author abdul.haseeb
 */
@Repository
public interface OrderRepository extends JpaRepository<Orders, Long> {
    
    List<Orders> findByUserId(Long userId);
    
}
