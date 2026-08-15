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
import com.souflow.models.requests.ReplyRequest;
import com.souflow.models.responses.AccountResponse;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ReplyResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminReplyController {

    private final ReplyService replyService;
    private final AccountService accountService;


    @PostMapping("/reply")
    ReplyResponse save(@RequestBody ReplyRequest request) {
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
        return replyService.save(request);
    }

    @DeleteMapping("/reply/{pk}")
    void deleteReplyByPk(@PathVariable Long pk) {
        replyService.softDeleteByPk(pk);
    }

    @GetMapping("/reply/{pk}")
    ReplyResponse findReplyByPk(@PathVariable Long pk) {
        return replyService.findByPk(pk);
    }

    @GetMapping("/reply")
    PageResponse<ReplyResponse> filterAndPaginateReplies(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDate fromDate,
            @RequestParam(required = false) LocalDate toDate,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return replyService.filterAndPaginateReply(keyword, fromDate, toDate, deleted, sortOrder, pageNumber, pageSize);
    }


}
