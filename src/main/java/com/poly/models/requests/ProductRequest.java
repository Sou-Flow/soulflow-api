package com.poly.models.requests;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductRequest {
	private Long pk;
	private String nameVn;
	private String nameEng;
	private String descriptionVn;
	private String descriptionEng;
	private BigDecimal price;
	private Boolean available;
	private Integer quantity;
	private Long categoryPk;
}
