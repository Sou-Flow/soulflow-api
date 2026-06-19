package com.souflow.comment.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReplyResponse {

  private Long id;
  private String content;
  private String authorName;
  private LocalDateTime createdDate;
}
