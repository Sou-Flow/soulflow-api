package com.poly.models.services;

import java.time.LocalDateTime;

import com.poly.models.enums.SortOrder;
import com.poly.models.requests.DiscountRequest;
import com.poly.models.responses.DiscountResponse;
import com.poly.models.responses.PageResponse;

public interface DiscountService {
	DiscountResponse save(DiscountRequest request);
	void softDeleteByPk(Long discountPk);
	DiscountResponse findByPk(Long discountPk);
	PageResponse<DiscountResponse> filterAndPaginateDiscounts(
			String keyword,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Boolean expired,
            Boolean deleted,
            SortOrder sortOrder,
            Integer pageNumber,
            Integer pageSize);
}
