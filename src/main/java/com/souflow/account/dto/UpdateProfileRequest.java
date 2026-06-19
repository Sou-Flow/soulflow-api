package com.souflow.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateProfileRequest {
  @NotBlank(message = "Họ tên không được để trống")
  private String fullName;

  @Email(message = "Email không hợp lệ")
  @NotBlank(message = "Email không được để trống")
  private String email;

  private String phoneNumber;
  private String address;
}
