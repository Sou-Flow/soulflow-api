package com.souflow.controllers;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.souflow.models.enums.SortOrder;
import com.souflow.models.requests.ProductImageRequest;
import com.souflow.models.requests.ProductRequest;
import com.souflow.models.responses.PageResponse;
import com.souflow.models.responses.ProductResponse;
import lombok.RequiredArgsConstructor;
import com.souflow.models.services.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final ImageService imageService;
    private final ProductImageService productImageService;

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

	@GetMapping("/product/detail/{pk}")
	ProductResponse findProductDetailByPk(@PathVariable Long pk) {
		return productService.findProductDetailByPk(pk);
	}

	@GetMapping("/product/by-code/{code}")
	ProductResponse findProductByCode(@PathVariable String code) {
		return productService.findProductByCode(code);
	}

	@GetMapping("/product")
	PageResponse<ProductResponse> filterAndPaginateProducts(
			@RequestParam(required = false) String keyword, 
			@RequestParam(required = false) BigDecimal minPrice, 
			@RequestParam(required = false) BigDecimal maxPrice, 
			@RequestParam(required = false) LocalDateTime fromDate,
			@RequestParam(required = false) LocalDateTime toDate,
			@RequestParam(required = false) Long categoryPk,
            @RequestParam(required = false) Boolean customised, 
			@RequestParam(defaultValue = "false") Boolean available,
			@RequestParam(defaultValue = "false") Boolean deleted,
			@RequestParam(defaultValue = "DESC") SortOrder sortOrder, 
			@RequestParam(defaultValue = "0") Integer pageNumber, 
			@RequestParam(defaultValue = "5") Integer pageSize
	) {
        return productService.filterAndPaginateProducts(keyword, minPrice, maxPrice, categoryPk, customised, available, deleted, fromDate, toDate, sortOrder, pageNumber, pageSize);
	}

}
