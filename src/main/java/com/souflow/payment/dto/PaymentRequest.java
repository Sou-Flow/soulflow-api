package com.souflow.payment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentRequest {

  @NotNull(message = "Order ID khong duoc de trong")
  private Long orderId;

  @NotNull(message = "So tien khong duoc de trong")
  @DecimalMin(value = "0.0", inclusive = false, message = "So tien phai lon hon 0")
  private BigDecimal amount;
}
