package com.poly.controllers;

import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.models.enums.OrderStatus;
import com.poly.models.enums.SortOrder;
import com.poly.models.requests.CartRequest;
import com.poly.models.requests.CommentRequest;
import com.poly.models.requests.OrderRequest;
import com.poly.models.requests.PaymentRequest;
import com.poly.models.requests.ReplyRequest;
import com.poly.models.responses.CartResponse;
import com.poly.models.responses.CommentResponse;
import com.poly.models.responses.OrderResponse;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.PaymentResponse;
import com.poly.models.responses.ReplyResponse;

import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final AdminController adminController;

    /* cart */

    @PostMapping("/cart")
    CartResponse saveCart(@RequestBody CartRequest request) {
        return adminController.save(request);
    }

    @DeleteMapping("/cart/{pk}")
    void deleteCartByPk(@PathVariable Long pk) {
        adminController.deleteCartByPk(pk);
    }

    @GetMapping("/cart/{pk}")
    CartResponse findCartByPk(@PathVariable Long pk) {
        return adminController.findCartByPk(pk);
    }

    @GetMapping("/cart")
    PageResponse<CartResponse> filterAndPaginateCarts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) LocalDateTime fromDate,
        @RequestParam(required = false) LocalDateTime toDate,
        @RequestParam(defaultValue = "false") Boolean expired,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return adminController.filterAndPaginateCarts(keyword, fromDate, toDate, expired, deleted, sortOrder, pageNumber, pageSize);
    }


    /* comment */

    @PostMapping("/comment")
    CommentResponse saveComment(@RequestBody CommentRequest request) {
        return adminController.save(request);
    }

    @DeleteMapping("/comment/{pk}")
    void deleteCommentByPk(@PathVariable Long pk) {
        adminController.deleteCommentByPk(pk);
    }

    @GetMapping("/comment/{pk}")
    CommentResponse findCommentByPk(@PathVariable Long pk) {
        return adminController.findCommentByPk(pk);
    }

    /* order */

    @PostMapping("/order")
	OrderResponse saveOrder(@RequestBody OrderRequest request) {
        request.setStatus(OrderStatus.PENDING);
        return adminController.save(request);
    }
	
	@DeleteMapping("/order/{pk}")
	void deleteOrderByPk(@PathVariable Long pk) {
        adminController.deleteOrderByPk(pk);
    }

	@GetMapping("/order/{pk}")
	OrderResponse findOrderByPk(@PathVariable Long pk) {
        return adminController.findOrderByPk(pk);
    }
	
	@GetMapping("/order")
	PageResponse<OrderResponse> filterAndPaginateOrders(
			@RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(defaultValue = "PENDING") OrderStatus status,
            @RequestParam(defaultValue = "false") Boolean expired,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
	) {
        return adminController.filterAndPaginateOrders(keyword, fromDate, toDate, status, expired, deleted, sortOrder, pageNumber, pageSize);
    }

    /* payment */

    @PostMapping("/payment")
    PaymentResponse savePayment(@RequestBody PaymentRequest request) {
        return adminController.save(request);
    }

    /* reply */

    @PostMapping("/reply")
    ReplyResponse saveReply(@RequestBody ReplyRequest request) {
        return adminController.save(request);
    }

    @DeleteMapping("/reply/{pk}")
    void deleteReplyByPk(@PathVariable Long pk) {
        adminController.deleteReplyByPk(pk);
    }

    @GetMapping("/reply/{pk}")
    ReplyResponse findReplyByPk(@PathVariable Long pk) {
        return adminController.findReplyByPk(pk);
    }
}
