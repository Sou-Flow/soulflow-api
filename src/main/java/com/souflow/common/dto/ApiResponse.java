package com.souflow.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

  private LocalDateTime timestamp;
  private int status;
  private String message;
  private String errorCode;
  private T data;

  public static <T> ApiResponse<T> success(int status, String message, T data) {
    return ApiResponse.<T>builder()
        .timestamp(LocalDateTime.now())
        .status(status)
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> error(int status, String errorCode, String message) {
    return ApiResponse.<T>builder()
        .timestamp(LocalDateTime.now())
        .status(status)
        .errorCode(errorCode)
        .message(message)
        .build();
  }
}
