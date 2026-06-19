package com.souflow.cart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCartItemRequest {

  @NotNull(message = "Product ID khong duoc de trong")
  private Long productId;

  @Min(value = 1, message = "So luong phai lon hon 0")
  private int quantity;
}
