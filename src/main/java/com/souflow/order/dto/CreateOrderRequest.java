package com.souflow.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateOrderRequest {

  @NotNull(message = "Cart ID khong duoc de trong")
  private Long cartId;

  @NotBlank(message = "Ho ten khong duoc de trong")
  @Size(max = 100)
  private String fullname;

  @NotBlank(message = "So dien thoai khong duoc de trong")
  @Size(max = 12)
  private String phoneNumber;

  @NotBlank(message = "Dia chi khong duoc de trong")
  @Size(max = 100)
  private String address;
}
