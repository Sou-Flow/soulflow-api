package com.poly.models.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poly.models.entities.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
}
