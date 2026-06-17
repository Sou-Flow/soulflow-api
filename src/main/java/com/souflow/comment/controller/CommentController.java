package com.souflow.comment.controller;

import com.souflow.comment.dto.CommentRequest;
import com.souflow.comment.dto.CommentResponse;
import com.souflow.comment.dto.ReplyRequest;
import com.souflow.comment.dto.ReplyResponse;
import com.souflow.comment.service.CommentService;
import com.souflow.common.dto.ApiResponse;
import com.souflow.security.AccountUserDetails;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

  private final CommentService commentService;

  @GetMapping("/product/{productId}")
  public ResponseEntity<ApiResponse<List<CommentResponse>>> findByProduct(
      @PathVariable Long productId) {
    List<CommentResponse> comments = commentService.findByProduct(productId);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Lay danh sach binh luan thanh cong", comments));
  }

  @PostMapping
  public ResponseEntity<ApiResponse<CommentResponse>> create(
      @AuthenticationPrincipal AccountUserDetails userDetails,
      @Valid @RequestBody CommentRequest request) {
    CommentResponse comment = commentService.create(request, userDetails.getAccount());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(HttpStatus.CREATED.value(), "Tao binh luan thanh cong", comment));
  }

  @PostMapping("/{commentId}/replies")
  public ResponseEntity<ApiResponse<ReplyResponse>> createReply(
      @AuthenticationPrincipal AccountUserDetails userDetails,
      @PathVariable Long commentId,
      @Valid @RequestBody ReplyRequest request) {
    ReplyResponse reply = commentService.createReply(commentId, request, userDetails.getAccount());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(HttpStatus.CREATED.value(), "Tra loi binh luan thanh cong", reply));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
    commentService.deleteComment(id);
    return ResponseEntity.ok(
        ApiResponse.success(HttpStatus.OK.value(), "Xoa binh luan thanh cong", null));
  }
}
