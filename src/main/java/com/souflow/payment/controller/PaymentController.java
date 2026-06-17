package com.souflow.payment.controller;

import com.souflow.common.dto.ApiResponse;
import com.souflow.payment.dto.PaymentRequest;
import com.souflow.payment.dto.PaymentResponse;
import com.souflow.payment.service.PaymentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

  private final PaymentService paymentService;

  @GetMapping("/order/{orderId}")
  public ResponseEntity<ApiResponse<List<PaymentResponse>>> findByOrder(
      @PathVariable Long orderId) {
    List<PaymentResponse> payments = paymentService.findByOrderId(orderId);
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Lay danh sach thanh toan thanh cong", payments));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<PaymentResponse>> create(
      @Valid @RequestBody PaymentRequest request) {
    PaymentResponse payment = paymentService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Thanh toan thanh cong", payment));
  }
}
