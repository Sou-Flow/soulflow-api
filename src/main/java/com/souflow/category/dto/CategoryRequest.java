package com.souflow.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

  @NotBlank(message = "Ten tieng Viet khong duoc de trong")
  @Size(max = 100)
  private String nameVn;

  @NotBlank(message = "Ten tieng Anh khong duoc de trong")
  @Size(max = 100)
  private String nameEng;

  @Size(max = 255)
  private String descriptionVn;

  @Size(max = 255)
  private String descriptionEng;
}
