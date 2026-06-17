package com.souflow.payment.service;

import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.order.entity.Order;
import com.souflow.order.repository.OrderRepository;
import com.souflow.payment.dto.PaymentRequest;
import com.souflow.payment.dto.PaymentResponse;
import com.souflow.payment.entity.Payment;
import com.souflow.payment.repository.PaymentRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;

  @Transactional(readOnly = true)
  public List<PaymentResponse> findByOrderId(Long orderId) {
    return paymentRepository.findByOrderId(orderId).stream().map(this::mapToResponse).toList();
  }

  @Transactional
  public PaymentResponse create(PaymentRequest request) {
    log.info("Creating payment for orderId={}", request.getOrderId());
    Order order =
        orderRepository
            .findByIdAndDeletedFalse(request.getOrderId())
            .orElseThrow(() -> new ResourceNotFoundException("Don hang khong ton tai"));

    Payment payment =
        Payment.builder()
            .order(order)
            .amount(request.getAmount())
            .paymentDate(LocalDateTime.now())
            .build();

    order.setStatus("PAID");
    orderRepository.save(order);

    return mapToResponse(paymentRepository.save(payment));
  }

  private PaymentResponse mapToResponse(Payment payment) {
    return PaymentResponse.builder()
        .id(payment.getId())
        .orderId(payment.getOrder().getId())
        .amount(payment.getAmount())
        .paymentDate(payment.getPaymentDate())
        .build();
  }
}
