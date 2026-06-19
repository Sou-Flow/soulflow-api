package com.souflow.category.dto;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponse implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long id;
  private String businessId;
  private String nameVn;
  private String nameEng;
  private String descriptionVn;
  private String descriptionEng;
}
