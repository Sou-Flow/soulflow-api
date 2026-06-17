package com.souflow.account.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

  @NotBlank(message = "Username khong duoc de trong")
  private String username;

  @NotBlank(message = "Mat khau khong duoc de trong")
  private String password;
}
