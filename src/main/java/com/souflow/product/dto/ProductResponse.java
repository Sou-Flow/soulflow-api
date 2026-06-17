package com.souflow.product.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductResponse {

  private Long id;
  private String businessId;
  private String nameVn;
  private String nameEng;
  private String descriptionVn;
  private String descriptionEng;
  private BigDecimal price;
  private boolean available;
  private int quantity;
  private int sales;
  private Long categoryId;
  private String categoryNameVn;
  private LocalDateTime createdDate;
}
