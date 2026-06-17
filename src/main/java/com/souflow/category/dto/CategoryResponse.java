package com.souflow.category.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {

  private Long id;
  private String businessId;
  private String nameVn;
  private String nameEng;
  private String descriptionVn;
  private String descriptionEng;
}
