package com.souflow.controllers;

import java.time.LocalDate;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.CommentRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.CommentResponse;
import com.souflow.models.responses.PageResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentService commentService;
    private final AccountService accountService;


    @PostMapping("/comment")
    CommentResponse save(@RequestBody CommentRequest request) {
        try {
            String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
            if (username != null && !username.equals("anonymousUser")) {
                AccountResponse acc = accountService.findByUsername(username);
                if (acc != null && acc.getPk() != null) {
                    request.setAccountPk(Long.valueOf(acc.getPk()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return commentService.save(request);
    }

    @DeleteMapping("/comment/{pk}")
    void deleteCommentByPk(@PathVariable Long pk) {
        commentService.softDeleteByPk(pk);
    }

    @GetMapping("/comment/{pk}")
    CommentResponse findCommentByPk(@PathVariable Long pk) {
        return commentService.findByPk(pk);
    }

    @GetMapping("/comment")
    PageResponse<CommentResponse> filterAndPaginateComments(
        @RequestParam(required = false) String keyword, 
		@RequestParam(required = false) LocalDate fromDate,
		@RequestParam(required = false) LocalDate toDate, 
		@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
		@RequestParam(defaultValue = "false") Boolean deleted, 
		@RequestParam(defaultValue = "0") Integer pageNumber, 
		@RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return commentService.filterAndPaginateComments(keyword, fromDate, sortOrder, toDate, deleted, pageNumber, pageSize);
    }


}
