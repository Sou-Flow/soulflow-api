package com.poly.models.services;

import java.time.LocalDate;
import java.util.List;

import com.poly.models.enums.SortOrder;
import com.poly.models.requests.ReplyRequest;
import com.poly.models.responses.PageResponse;
import com.poly.models.responses.ReplyResponse;

public interface ReplyService {
	ReplyResponse save(ReplyRequest request);
	void softDeleteByPk(Long replyPk);
	ReplyResponse findByPk(Long replyPk);
	List<ReplyResponse> findAll();
	PageResponse<ReplyResponse> filterAndPaginateReply(
			String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            Boolean deleted,
			SortOrder sortOrder,
            Integer pageNumber,
			Integer pageSize
	);
}
