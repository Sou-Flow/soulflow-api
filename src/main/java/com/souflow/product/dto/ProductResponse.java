package com.souflow.product.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse implements Serializable {

  private static final long serialVersionUID = 1L;

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
