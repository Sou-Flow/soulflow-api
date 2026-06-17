package com.souflow.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

  @NotBlank(message = "Username khong duoc de trong")
  @Size(max = 50, message = "Username toi da 50 ky tu")
  private String username;

  @NotBlank(message = "Mat khau khong duoc de trong")
  @Size(min = 6, max = 100, message = "Mat khau tu 6 den 100 ky tu")
  private String password;

  @NotBlank(message = "Ho ten khong duoc de trong")
  @Size(max = 50, message = "Ho ten toi da 50 ky tu")
  private String fullname;

  @NotBlank(message = "Email khong duoc de trong")
  @Email(message = "Email khong hop le")
  @Size(max = 50, message = "Email toi da 50 ky tu")
  private String email;

  private String phoneNumber;
  private String address;
}
