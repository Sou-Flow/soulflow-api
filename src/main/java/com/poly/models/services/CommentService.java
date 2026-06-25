package com.poly.models.services;

import java.time.LocalDate;

import com.poly.models.enums.SortOrder;
import com.poly.models.requests.CommentRequest;
import com.poly.models.responses.CommentResponse;
import com.poly.models.responses.PageResponse;

public interface CommentService {
	CommentResponse save(CommentRequest request);
	void softDeleteByPk(Long commentPk);
	CommentResponse findByPk(Long commentPk);
	PageResponse<CommentResponse> filterAndPaginateComments(
		String keyword, 
		LocalDate fromDate,
		SortOrder sortOrder, 
		LocalDate toDate, 
		Boolean deleted, 
		Integer pageNumber, 
		Integer pageSize);
}
