package com.souflow.order.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderDetailResponse {

  private Long id;
  private Long productId;
  private String productNameVn;
  private String productNameEng;
  private BigDecimal productPrice;
  private int quantity;
  private BigDecimal subtotal;
}
