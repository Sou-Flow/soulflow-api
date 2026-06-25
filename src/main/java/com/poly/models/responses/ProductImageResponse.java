package com.poly.models.responses;


import lombok.Data;

@Data
public class ProductImageResponse {
	
	private String pk;
	
	private String name;
	
	private String url;

	private String createdDate;
	
	private String productPk;
}
