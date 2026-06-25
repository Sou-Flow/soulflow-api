package com.poly.models.services;


import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.poly.models.enums.SortOrder;
import com.poly.models.requests.ProductRequest;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.ProductResponse;

public interface ProductService {
	ProductResponse save(ProductRequest request);
	void softDeleteByPk(Long productPk);
	ProductResponse findByPk(Long productPk);
	ProductResponse findProductDetailByPk(Long productPk);
	PageResponse<ProductResponse> filterAndPaginateProducts(
			String keyword, 
			BigDecimal minPrice, 
			BigDecimal maxPrice, 
			Long categoryPk, 
			Boolean available,
			Boolean deleted,
			LocalDateTime fromDate,
			LocalDateTime toDate,
			SortOrder sortOrder, 
			Integer pageNumber, 
			Integer pageSize);
}
