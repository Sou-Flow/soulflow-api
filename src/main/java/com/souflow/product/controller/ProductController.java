package com.souflow.product.controller;

import com.souflow.common.dto.ApiResponse;
import com.souflow.product.dto.ProductRequest;
import com.souflow.product.dto.ProductResponse;
import com.souflow.product.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductService productService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<ProductResponse>>> findAll(
      @RequestParam(required = false) Long categoryId) {
    List<ProductResponse> products =
        categoryId != null ? productService.findByCategory(categoryId) : productService.findAll();
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh sach san pham thanh cong", products));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> findById(@PathVariable Long id) {
    ProductResponse product = productService.findById(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay san pham thanh cong", product));
  }

  @GetMapping("/by-code/{code}")
  public ResponseEntity<ApiResponse<ProductResponse>> findByCode(@PathVariable String code) {
    ProductResponse product = productService.findByCode(code);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay san pham thanh cong", product));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<ProductResponse>> create(
      @Valid @RequestBody ProductRequest request) {
    ProductResponse product = productService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tao san pham thanh cong", product));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<ProductResponse>> update(
      @PathVariable Long id, @Valid @RequestBody ProductRequest request) {
    ProductResponse product = productService.update(id, request);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Cap nhat san pham thanh cong", product));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa san pham thanh cong", null));
  }
}
