package com.souflow.comment.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponse {

  private Long id;
  private String content;
  private String authorName;
  private LocalDateTime createdDate;
  private List<ReplyResponse> replies;
}
