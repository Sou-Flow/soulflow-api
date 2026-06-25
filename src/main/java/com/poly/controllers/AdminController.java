package com.poly.controllers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.poly.models.enums.OrderStatus;
import com.poly.models.enums.RoleCode;
import com.poly.models.enums.SortOrder;
import com.poly.models.requests.AccountRequest;
import com.poly.models.requests.AuthRequest;
import com.poly.models.requests.CartRequest;
import com.poly.models.requests.CategoryRequest;
import com.poly.models.requests.CommentRequest;
import com.poly.models.requests.DiscountRequest;
import com.poly.models.requests.OrderRequest;
import com.poly.models.requests.PaymentRequest;
import com.poly.models.requests.ProductImageRequest;
import com.poly.models.requests.ProductRequest;
import com.poly.models.requests.ReplyRequest;
import com.poly.models.responses.AccountResponse;
import com.poly.models.responses.AuthResponse;
import com.poly.models.responses.CartResponse;
import com.poly.models.responses.CategoryResponse;
import com.poly.models.responses.CommentResponse;
import com.poly.models.responses.DiscountResponse;
import com.poly.models.responses.OrderResponse;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.PaymentResponse;
import com.poly.models.responses.ProductImageResponse;
import com.poly.models.responses.ProductResponse;
import com.poly.models.responses.ReplyResponse;
import com.poly.models.services.BaseService;
import com.poly.models.services.impl.AccountServiceImpl.GoogleTokenDTO;


@RestController
@RequestMapping("/admin")
public class AdminController extends BaseService {

    /* account */

    @PostMapping("/login")
    AuthResponse login(@RequestBody AuthRequest request) {
        return accountService.login(request);
    }

    @PostMapping("/google/login")
    AuthResponse googleLogin(@RequestBody GoogleTokenDTO token) {
        return accountService.loginWithGoogle(token);
    }

    @PostMapping("/account")
    AccountResponse save(
        @RequestPart("account") AccountRequest request,
        @RequestPart(value = "file", required = false) MultipartFile file) throws Exception {
        
        if (file != null) {
            request.setPhoto(imageService.upload(file));
        }

        return accountService.save(request);
    }

    @DeleteMapping("/account/{pk}")
    void deleteAccount(@PathVariable Long pk) {
        accountService.softDeleteByPk(pk);
    }

    @GetMapping("/account/{pk}")
    AccountResponse findAccountByPk(@PathVariable Long pk) {
        return accountService.findByPk(pk);
    }

    @GetMapping("/account/by-username/{username}")
    AccountResponse findAccountByUsername(@PathVariable String username) {
        return accountService.findByUsername(username);
    }

    @GetMapping("/account/by-email/{email}")
    AccountResponse findAccountByEmail(@PathVariable String email) {
        return accountService.findByEmail(email);
    }

    @GetMapping("/account")
    PageResponse<AccountResponse> filterAndPaginateAccounts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) LocalDateTime fromDate,
        @RequestParam(required = false) LocalDateTime toDate,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "false") Boolean disabled,
        @RequestParam(defaultValue = "ALL") RoleCode role,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "5") Integer pageSize
        ) {

        return accountService.filterAndPaginateAccounts(deleted, keyword, fromDate, toDate, disabled, role, sortOrder, pageNumber, pageSize);
    }

    @PostMapping("/category")
    CategoryResponse save(@RequestBody CategoryRequest request) {
        return categoryService.save(request);
    }

    @DeleteMapping("/category/{pk}")
    void deleteCategoryByPk(@PathVariable Long pk) {
        categoryService.softDeleteByPk(pk);
    }

    @GetMapping("/category/{pk}")
    CategoryResponse findCaregoryByPk(@PathVariable Long pk) {
        return categoryService.findByPk(pk);
    }

    @GetMapping("/category")
    PageResponse<CategoryResponse> filterAndPaginateCategories(
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "false") Boolean deleted,
        @RequestParam(defaultValue = "0") Integer pageNumber,
        @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
        @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return categoryService.filterAndPaginateCategories(keyword, deleted, sortOrder, pageNumber, pageSize);
    }

    @GetMapping("/category/list")
    List<CategoryResponse> findCategoryList() {
        return categoryService.findAll();
    }

    
    /* discount */

    @PostMapping("/discount")
    DiscountResponse save(@RequestBody DiscountRequest request) {
        return discountService.save(request);
    }

    @DeleteMapping("/discount/{pk}")
    void deleteDiscountByPk(@PathVariable Long pk) {
        discountService.softDeleteByPk(pk);
    }

    @GetMapping("/discount/{pk}")
    DiscountResponse findDiscountByPk(@PathVariable Long pk) {
        return discountService.findByPk(pk);
    }

    @GetMapping("/discount")
    PageResponse<DiscountResponse> filterAndPaginateDiscounts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) LocalDateTime fromDate,
            @RequestParam(required = false) LocalDateTime toDate,
            @RequestParam(defaultValue = "false") Boolean expired,
            @RequestParam(defaultValue = "false") Boolean deleted,
            @RequestParam(defaultValue = "DESC") SortOrder sortOrder,
            @RequestParam(defaultValue = "0") Integer pageNumber,
            @RequestParam(defaultValue = "5") Integer pageSize
    ) {
        return discountService.filterAndPaginateDiscounts(keyword, fromDate, toDate, expired, deleted, sortOrder, pageNumber, pageSize);
    }


    /* product */
    @PostMapping("/product")
	ProductResponse save(
			@RequestPart("request") ProductRequest request,
			@RequestPart(value = "files", required = false) MultipartFile[] files
			) throws Exception {

		// Save the product first to get its generated pk
		ProductResponse saved = productService.save(request);

		// Then associate uploaded images using the real pk
		try {
            if (files != null && files.length > 0) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {

                    String name = imageService.upload(file);
                    ProductImageRequest productImageRequest = new ProductImageRequest();
                    productImageRequest.setName(name);
                    productImageRequest.setProductPk(Long.valueOf(saved.getPk()));
                    productImageRequest.setDeleted(false);
                    productImageRequest.setProductPk(Long.valueOf(saved.getPk()));
                    productImageService.save(productImageRequest);
                }
            }
        }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        
		return saved;
	}
	
	@DeleteMapping("/product/{pk}")
	void deleteProductByPk(@PathVariable Long pk) {
		productService.softDeleteByPk(pk);
	}

	@GetMapping("/product/{pk}")
	ProductResponse findProductByPk(@PathVariable Long pk) {
		return productService.findByPk(pk);
	}

	@GetMapping("/product")
	PageResponse<ProductResponse> filterAndPaginateProducts(
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) BigDecimal minPrice, 
			@RequestParam(required = false) BigDecimal maxPrice, 
			@RequestParam(required = false) LocalDateTime fromDate,
			@RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(required = false) Long categoryPk, 
			@RequestParam(defaultValue = "false") Boolean available,
			@RequestParam(defaultValue = "false") Boolean deleted,
			@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
			@RequestParam(defaultValue = "0") Integer pageNumber, 
			@RequestParam(defaultValue = "5") Integer pageSize
	) {
        return productService.filterAndPaginateProducts(keyword, minPrice, maxPrice, categoryPk, available, deleted, fromDate, toDate, sortOrder, pageNumber, pageSize);
	}

    /* reply */

    @PostMapping("/reply")
    ReplyResponse save(@RequestBody ReplyRequest request) {
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


    /* comment */

    @PostMapping("/comment")
    CommentResponse save(@RequestBody CommentRequest request) {
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


    /* order */

    @PostMapping("/order")
	OrderResponse save(@RequestBody OrderRequest request) {
		return orderService.save(request);
	}
	
	@DeleteMapping("/order/{pk}")
	void deleteOrderByPk(@PathVariable Long pk) {
		orderService.softDeleteByPk(pk);
	}

	@GetMapping("/order/{pk}")
	OrderResponse findOrderByPk(@PathVariable Long pk) {
		return orderService.findByPk(pk);
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
		
		return orderService.filterAndPaginateOrders(keyword, fromDate, toDate, status, expired, deleted, sortOrder, pageNumber, pageSize);
	}


    /* cart */

    @PostMapping("/cart")
    CartResponse save(@RequestBody CartRequest request) {
        return cartService.save(request);
    }

    @DeleteMapping("/cart/{pk}")
    void deleteCartByPk(@PathVariable Long pk) {
        cartService.softDeleteByPk(pk);
    }

    @GetMapping("/cart/{pk}")
    CartResponse findCartByPk(@PathVariable Long pk) {
        return cartService.findByPk(pk);
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
        return cartService.filterAndPaginateCarts(keyword, fromDate, toDate, expired, deleted, sortOrder, pageNumber, pageSize);
    }

    /* payment */

    @PostMapping("/payment")
    PaymentResponse save(@RequestBody PaymentRequest request) {
        return paymentService.save(request);
    }

    /* image storing */

    @GetMapping("/download/image")
    InputStream downloadImage(String imageName) throws Exception {
        return imageService.download(imageName);
    }
}
