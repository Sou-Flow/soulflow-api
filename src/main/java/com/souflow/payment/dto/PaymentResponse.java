package com.souflow.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentResponse {

  private Long id;
  private Long orderId;
  private BigDecimal amount;
  private LocalDateTime paymentDate;
}
