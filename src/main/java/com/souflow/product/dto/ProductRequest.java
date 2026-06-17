package com.souflow.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequest {

  @NotBlank(message = "Ten tieng Viet khong duoc de trong")
  @Size(max = 100)
  private String nameVn;

  @NotBlank(message = "Ten tieng Anh khong duoc de trong")
  @Size(max = 100)
  private String nameEng;

  @NotBlank(message = "Mo ta tieng Viet khong duoc de trong")
  @Size(max = 255)
  private String descriptionVn;

  @NotBlank(message = "Mo ta tieng Anh khong duoc de trong")
  @Size(max = 255)
  private String descriptionEng;

  @NotNull(message = "Gia khong duoc de trong")
  @DecimalMin(value = "0.0", inclusive = false, message = "Gia phai lon hon 0")
  private BigDecimal price;

  @NotNull(message = "Category ID khong duoc de trong")
  private Long categoryId;

  @Min(value = 0, message = "So luong khong duoc am")
  private int quantity;

  private boolean available = true;
}
