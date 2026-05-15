/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.microservice.microauth.repository;

import com.microservice.microauth.entity.Role;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 *
 * @author abdul.haseeb
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Integer>{
    
    Optional<Role> findByName(String name);
    
}
