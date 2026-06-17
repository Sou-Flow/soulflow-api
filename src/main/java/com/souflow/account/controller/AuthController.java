package com.souflow.account.controller;

import com.souflow.account.dto.AccountResponse;
import com.souflow.account.dto.AuthResponse;
import com.souflow.account.dto.LoginRequest;
import com.souflow.account.dto.RegisterRequest;
import com.souflow.account.service.AuthService;
import com.souflow.common.dto.ApiResponse;
import com.souflow.security.AccountUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse<AuthResponse>> register(
      @Valid @RequestBody RegisterRequest request) {
    AuthResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Dang ky thanh cong", response));
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse response = authService.login(request);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Dang nhap thanh cong", response));
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<AccountResponse>> getCurrentAccount(
      @AuthenticationPrincipal AccountUserDetails userDetails) {
    AccountResponse response = authService.getCurrentAccount(userDetails);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay thong tin tai khoan thanh cong", response));
  }
}
