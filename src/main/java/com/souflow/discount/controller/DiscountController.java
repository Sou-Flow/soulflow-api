package com.souflow.discount.controller;

import com.souflow.common.dto.ApiResponse;
import com.souflow.discount.dto.DiscountRequest;
import com.souflow.discount.dto.DiscountResponse;
import com.souflow.discount.service.DiscountService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/discounts")
@RequiredArgsConstructor
public class DiscountController {

  private final DiscountService discountService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<DiscountResponse>>> findActive() {
    List<DiscountResponse> discounts = discountService.findActive();
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh sach giam gia thanh cong", discounts));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<DiscountResponse>> findById(@PathVariable Long id) {
    DiscountResponse discount = discountService.findById(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay ma giam gia thanh cong", discount));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<DiscountResponse>> create(
      @Valid @RequestBody DiscountRequest request) {
    DiscountResponse discount = discountService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                HttpStatus.CREATED.value(), "Tao ma giam gia thanh cong", discount));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    discountService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa ma giam gia thanh cong", null));
  }
}
