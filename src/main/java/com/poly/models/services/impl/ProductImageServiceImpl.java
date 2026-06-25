package com.poly.models.services.impl;

import com.poly.models.entities.ProductImage;
import com.poly.models.mappers.ProductImageMapper;
import com.poly.models.repositories.ProductImageRepository;
import com.poly.models.requests.ProductImageRequest;
import com.poly.models.responses.ProductImageResponse;
import com.poly.models.services.ProductImageService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductImageServiceImpl implements ProductImageService {

	private final ProductImageMapper productImageMapper;
	
	private final ProductImageRepository productImageRepo;

	@Override
	public ProductImageResponse save(ProductImageRequest request) {
		// TODO Auto-generated method stub
		ProductImage productImage = productImageMapper.toEntity(request);
		ProductImage saved = productImageRepo.save(productImage);
		return productImageMapper.toResponse(saved);
	}

	@Override
	public void softDeleteByPk(Long pk) {
		// TODO Auto-generated method stub
		ProductImage productImage = productImageRepo.findById(pk)
				.orElseThrow(() -> new EntityNotFoundException("Product Image not found with pk: " + pk));
		productImage.setDeleted(true);
		productImageRepo.save(productImage);
	}

}
