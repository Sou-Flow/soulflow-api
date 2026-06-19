package com.souflow.comment.service;

import com.souflow.account.entity.Account;
import com.souflow.comment.dto.CommentRequest;
import com.souflow.comment.dto.CommentResponse;
import com.souflow.comment.dto.ReplyRequest;
import com.souflow.comment.dto.ReplyResponse;
import com.souflow.comment.entity.Comment;
import com.souflow.comment.entity.Reply;
import com.souflow.comment.repository.CommentRepository;
import com.souflow.comment.repository.ReplyRepository;
import com.souflow.common.exception.ResourceNotFoundException;
import com.souflow.product.entity.Product;
import com.souflow.product.service.ProductService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final ReplyRepository replyRepository;
  private final ProductService productService;

  @Transactional(readOnly = true)
  public List<CommentResponse> findByProduct(Long productId) {
    return commentRepository
        .findByProductIdAndDeletedFalseOrderByCreatedDateDesc(productId)
        .stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional
  public CommentResponse create(CommentRequest request, Account account) {
    log.info("Creating comment for productId={}", request.getProductId());
    Product product = productService.getEntityById(request.getProductId());

    Comment comment =
        Comment.builder()
            .content(request.getContent())
            .product(product)
            .account(account)
            .createdDate(LocalDateTime.now())
            .build();

    return mapToResponse(commentRepository.save(comment));
  }

  @Transactional
  public ReplyResponse createReply(Long commentId, ReplyRequest request, Account account) {
    Comment comment =
        commentRepository
            .findByIdAndDeletedFalse(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Binh luan khong ton tai"));

    Reply reply =
        Reply.builder()
            .content(request.getContent())
            .comment(comment)
            .account(account)
            .createdDate(LocalDateTime.now())
            .build();

    return mapToReplyResponse(replyRepository.save(reply));
  }

  @Transactional
  public void deleteComment(Long id) {
    Comment comment =
        commentRepository
            .findByIdAndDeletedFalse(id)
            .orElseThrow(() -> new ResourceNotFoundException("Binh luan khong ton tai"));
    comment.setDeleted(true);
    commentRepository.save(comment);
  }

  private CommentResponse mapToResponse(Comment comment) {
    List<ReplyResponse> replies =
        replyRepository
            .findByCommentIdAndDeletedFalseOrderByCreatedDateAsc(comment.getId())
            .stream()
            .map(this::mapToReplyResponse)
            .toList();

    return CommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .authorName(comment.getAccount().getFullName())
        .createdDate(comment.getCreatedDate())
        .replies(replies)
        .build();
  }

  private ReplyResponse mapToReplyResponse(Reply reply) {
    return ReplyResponse.builder()
        .id(reply.getId())
        .content(reply.getContent())
        .authorName(reply.getAccount().getFullName())
        .createdDate(reply.getCreatedDate())
        .build();
  }
}
