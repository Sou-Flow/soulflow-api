package com.souflow.cart.controller;

import com.souflow.cart.dto.AddCartItemRequest;
import com.souflow.cart.dto.CartResponse;
import com.souflow.cart.service.CartService;
import com.souflow.common.dto.ApiResponse;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

  private final CartService cartService;

  @GetMapping
  public ResponseEntity<ApiResponse<List<CartResponse>>> findMyCarts(
      @AuthenticationPrincipal AccountUserDetails userDetails) {
    List<CartResponse> carts = cartService.findByAccount(userDetails.getAccount());
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh sach gio hang thanh cong", carts));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<CartResponse>> findById(@PathVariable Long id) {
    CartResponse cart = cartService.findById(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay gio hang thanh cong", cart));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CartResponse>> createCart(
      @AuthenticationPrincipal AccountUserDetails userDetails) {
    CartResponse cart = cartService.createCart(userDetails.getAccount());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tao gio hang thanh cong", cart));
  }

  @PostMapping("/{id}/items")
  public ResponseEntity<ApiResponse<CartResponse>> addItem(
      @PathVariable Long id, @Valid @RequestBody AddCartItemRequest request) {
    CartResponse cart = cartService.addItem(id, request);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Them san pham vao gio hang thanh cong", cart));
  }

  @DeleteMapping("/{cartId}/items/{itemId}")
  public ResponseEntity<ApiResponse<Void>> removeItem(
      @PathVariable Long cartId, @PathVariable Long itemId) {
    cartService.removeItem(cartId, itemId);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa san pham khoi gio hang thanh cong", null));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteCart(@PathVariable Long id) {
    cartService.deleteCart(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa gio hang thanh cong", null));
  }
}
