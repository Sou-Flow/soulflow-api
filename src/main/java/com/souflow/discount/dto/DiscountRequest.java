package com.souflow.discount.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiscountRequest {

  @Min(value = 0, message = "Phan tram giam gia khong duoc am")
  @Max(value = 100, message = "Phan tram giam gia toi da 100")
  private Double percentage;

  @Size(max = 255)
  private String descriptionVn;

  @Size(max = 255)
  private String descriptionEng;

  @NotNull(message = "Ngay het han khong duoc de trong")
  private LocalDateTime expiredDate;
}
