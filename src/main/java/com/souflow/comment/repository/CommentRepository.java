package com.souflow.comment.repository;

import com.souflow.comment.entity.Comment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  List<Comment> findByProductIdAndDeletedFalseOrderByCreatedDateDesc(Long productId);

  Optional<Comment> findByIdAndDeletedFalse(Long id);
}
