package com.souflow.order.controller;

import com.souflow.common.dto.ApiResponse;
import com.souflow.order.dto.CreateOrderRequest;
import com.souflow.order.dto.OrderResponse;
import com.souflow.order.service.OrderService;
import com.souflow.security.AccountUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<OrderResponse>>> findMyOrders(
      @AuthenticationPrincipal AccountUserDetails userDetails) {
    List<OrderResponse> orders = orderService.findByAccount(userDetails.getAccount());
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh sach don hang thanh cong", orders));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<OrderResponse>> findById(@PathVariable Long id) {
    OrderResponse order = orderService.findById(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay don hang thanh cong", order));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @AuthenticationPrincipal AccountUserDetails userDetails,
      @Valid @RequestBody CreateOrderRequest request) {
    OrderResponse order = orderService.createFromCart(request, userDetails.getAccount());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Dat hang thanh cong", order));
  }

  @PutMapping("/{id}/status")
  public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
      @PathVariable Long id, @RequestParam String status) {
    OrderResponse order = orderService.updateStatus(id, status);
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Cap nhat trang thai don hang thanh cong", order));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    orderService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa don hang thanh cong", null));
  }
}
