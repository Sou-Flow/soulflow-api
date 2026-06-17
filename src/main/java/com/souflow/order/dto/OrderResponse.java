package com.souflow.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderResponse {

  private Long id;
  private String businessId;
  private String fullname;
  private String phoneNumber;
  private String address;
  private BigDecimal total;
  private String status;
  private boolean expired;
  private LocalDateTime createdDate;
  private LocalDateTime expiredDate;
  private List<OrderDetailResponse> details;
}
