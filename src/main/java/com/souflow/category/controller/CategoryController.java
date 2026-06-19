package com.souflow.category.controller;

import com.souflow.category.dto.CategoryRequest;
import com.souflow.category.dto.CategoryResponse;
import com.souflow.category.service.CategoryService;
import com.souflow.common.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> findAll() {
    List<CategoryResponse> categories = categoryService.findAll();
    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Lay danh sach danh muc thanh cong", categories));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> findById(@PathVariable Long id) {
    CategoryResponse category = categoryService.findById(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh muc thanh cong", category));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CategoryResponse>> create(
      @Valid @RequestBody CategoryRequest request) {
    CategoryResponse category = categoryService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tao danh muc thanh cong", category));
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<CategoryResponse>> update(
      @PathVariable Long id, @Valid @RequestBody CategoryRequest request) {
    CategoryResponse category = categoryService.update(id, request);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Cap nhat danh muc thanh cong", category));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    categoryService.delete(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa danh muc thanh cong", null));
  }
}
