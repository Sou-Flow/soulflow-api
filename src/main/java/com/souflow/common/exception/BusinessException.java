package com.souflow.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessException extends RuntimeException {

  private final ErrorCode errorCode;
  private final HttpStatus status;

  public BusinessException(ErrorCode errorCode, String message, HttpStatus status) {
    super(message);
    this.errorCode = errorCode;
    this.status = status;
  }

  public BusinessException(ErrorCode errorCode, String message) {
    this(errorCode, message, HttpStatus.BAD_REQUEST);
  }
}
