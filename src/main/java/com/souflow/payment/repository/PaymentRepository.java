package com.souflow.payment.repository;

import com.souflow.payment.entity.Payment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

  List<Payment> findByOrderId(Long orderId);
}
