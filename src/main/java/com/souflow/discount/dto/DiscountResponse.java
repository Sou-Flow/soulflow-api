package com.souflow.discount.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DiscountResponse {

  private Long id;
  private String businessId;
  private Double percentage;
  private String descriptionVn;
  private String descriptionEng;
  private LocalDateTime createdDate;
  private LocalDateTime expiredDate;
  private boolean expired;
}
