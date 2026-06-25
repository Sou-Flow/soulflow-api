package com.poly.models.services;

import com.poly.models.requests.ProductImageRequest;
import com.poly.models.responses.ProductImageResponse;

public interface ProductImageService {
	ProductImageResponse save(ProductImageRequest request);
	void softDeleteByPk(Long pk);
}
