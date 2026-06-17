package com.souflow.cart.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartResponse {

  private Long id;
  private String businessId;
  private BigDecimal total;
  private boolean expired;
  private LocalDateTime expiredDate;
  private LocalDateTime createdDate;
  private List<CartItemResponse> items;
}
