package com.poly.models.requests;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class DiscountRequest {

    private Long pk;
	
	private	BigDecimal percentage;
	
	private String descriptionVn;
	
	private String descriptionEng;
	
	private LocalDateTime expiredDate;

	private List<ProductRequest> productRequests;
}
