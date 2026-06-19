package com.souflow.account.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AccountResponse {

  private Long id;
  private String username;
  private String fullName;
  private String email;
  private String phoneNumber;
  private String address;
  private String photo;
  private String roleCode;
  private LocalDateTime createdDate;
}
