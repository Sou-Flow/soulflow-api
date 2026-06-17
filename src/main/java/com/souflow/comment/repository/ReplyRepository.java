package com.souflow.comment.repository;

import com.souflow.comment.entity.Reply;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  List<Reply> findByCommentIdAndDeletedFalseOrderByCreatedDateAsc(Long commentId);
}
