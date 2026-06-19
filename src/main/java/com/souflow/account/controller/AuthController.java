package com.souflow.account.controller;

import com.souflow.account.dto.AccountResponse;
import com.souflow.account.dto.AuthResponse;
import com.souflow.account.dto.ChangePasswordRequest;
import com.souflow.account.dto.ForgotPasswordRequest;
import com.souflow.account.dto.LoginRequest;
import com.souflow.account.dto.RegisterRequest;
import com.souflow.account.dto.ResetPasswordRequest;
import com.souflow.account.dto.UpdateProfileRequest;
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
import org.springframework.web.bind.annotation.PutMapping;
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

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestBody String entity) {
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Dang xuat thanh cong", null));
  }

  @PostMapping("/refresh-token")
  public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody String entity) {
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lam moi token thanh cong", null));
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @AuthenticationPrincipal AccountUserDetails userDetails,
      @Valid @RequestBody ChangePasswordRequest request) {

    authService.changePassword(userDetails, request.getCurrentPassword(), request.getNewPassword());

    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Doi mat khau thanh cong", null));
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse<String>> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {

    authService.forgotPassword(request);

    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Gui email thanh cong", request.getEmail()));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<ApiResponse<Void>> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {

    authService.resetPassword(request);

    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Dat lai mat khau thanh cong", null));
  }

  @PutMapping("/update-profile")
  public ResponseEntity<ApiResponse<Void>> updateProfile(
      @AuthenticationPrincipal AccountUserDetails userDetails,
      @Valid @RequestBody UpdateProfileRequest request) {

    authService.updateProfile(userDetails, request);

    return ResponseEntity.ok(
        ApiResponse.success(
            HttpStatus.OK.value(), "Cap nhat thong tin tai khoan thanh cong", null));
  }
}
