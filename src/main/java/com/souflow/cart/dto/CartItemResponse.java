package com.souflow.cart.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CartItemResponse {

  private Long id;
  private Long productId;
  private String productNameVn;
  private String productNameEng;
  private BigDecimal productPrice;
  private int quantity;
  private BigDecimal subtotal;
}
