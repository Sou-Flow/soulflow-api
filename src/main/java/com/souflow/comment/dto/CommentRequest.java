package com.souflow.comment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommentRequest {

  @NotNull(message = "Product ID khong duoc de trong")
  private Long productId;

  @NotBlank(message = "Noi dung khong duoc de trong")
  @Size(max = 500)
  private String content;
}
