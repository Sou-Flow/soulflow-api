package com.poly.controllers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.models.enums.SortOrder;
import com.poly.models.requests.AuthRequest;
import com.poly.models.responses.AuthResponse;
import com.poly.models.responses.CategoryResponse;
import com.poly.models.responses.CommentResponse;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.ProductResponse;
import com.poly.models.responses.ReplyResponse;
import com.poly.models.services.impl.AccountServiceImpl.GoogleTokenDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class NonUserController  {

    private final AdminController adminController;

    @PostMapping("/login")
    AuthResponse login(@RequestBody AuthRequest request) {
        return adminController.login(request);
    }

    @PostMapping("/google/login")
    AuthResponse googleLogin(@RequestBody GoogleTokenDTO token) {
        return adminController.googleLogin(token);
    }

    @GetMapping("/category/list")
    List<CategoryResponse> findAll() {
        return adminController.findCategoryList();
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
        return adminController.filterAndPaginateComments(keyword, fromDate, toDate, sortOrder, deleted, pageNumber, pageSize);
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
        return adminController.filterAndPaginateReplies(keyword, fromDate, toDate, deleted, sortOrder, pageNumber, pageSize);
    }

    @GetMapping("/product")
	PageResponse<ProductResponse> filterAndPaginateProducts(
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) BigDecimal minPrice, 
			@RequestParam(required = false) BigDecimal maxPrice, 
			@RequestParam(required = false) LocalDateTime fromDate,
			@RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(required = false) Long categoryPk, 
            @RequestParam(defaultValue = "false") Boolean customised,
			@RequestParam(defaultValue = "false") Boolean available,
			@RequestParam(defaultValue = "false") Boolean deleted,
			@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
			@RequestParam(defaultValue = "0") Integer pageNumber, 
			@RequestParam(defaultValue = "5") Integer pageSize
	) {
        return adminController.filterAndPaginateProducts(keyword, minPrice, maxPrice, fromDate, toDate, categoryPk, customised, available, deleted, sortOrder, pageNumber, pageSize);
	}

}
