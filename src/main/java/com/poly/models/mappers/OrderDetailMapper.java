package com.poly.models.mappers;

import java.math.BigDecimal;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.poly.models.entities.Product;
import com.poly.models.entities.OrderDetail;
import com.poly.models.repositories.ProductRepository;
import com.poly.models.requests.OrderDetailRequest;
import com.poly.models.responses.OrderDetailResponse;
import com.poly.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Component
@Mapper(componentModel = "spring")
public abstract class OrderDetailMapper {
	
	@Autowired
	protected ProductRepository productRepo;
	@Autowired
    protected LocaleUtil localeHelper;

	@Mapping(target = "nameVn", 		ignore = true)
	@Mapping(target = "nameEng", 		ignore = true)
	@Mapping(target = "price", 			ignore = true)
	@Mapping(target = "subtotal", 		ignore = true)
	@Mapping(target = "product", 		ignore = true)
	@Mapping(target = "order", 			ignore = true)
	public abstract OrderDetail toEntity(OrderDetailRequest request);

	@Mapping(target = "price", 			source = "price", numberFormat = "#.##")
	@Mapping(target = "subtotal", 		source = "subtotal", numberFormat = "#.##")
	@Mapping(target = "name" , 			expression = "java(localeHelper.getName(orderDetail.product.getNameVn(), orderDetail.product.getNameEng()))")
	@Mapping(target = "productPk" ,		source = "product.pk")
	@Mapping(target = "orderPk" , 		source = "order.pk")
	public abstract OrderDetailResponse toResponse(OrderDetail orderDetail);

	public abstract List<OrderDetail> toEntityList(List<OrderDetailRequest> orderDetailRequests);

	public abstract List<OrderDetailResponse> toResponseList(List<OrderDetail> orderDetails);

	@AfterMapping
	protected void afterToEntity(OrderDetailRequest request, @MappingTarget OrderDetail orderDetail) {
		
		Long productPk = request.getProductPk();
		Product product = productRepo.findById(productPk)
			.orElseThrow(() -> new EntityNotFoundException("Can't found product with PK: " + productPk));
		
		Integer effectedRows = productRepo.decreaseQuantity(request.getProductPk(), request.getQuantity());
		if (effectedRows == 0) {
			throw new RuntimeException("Quantity is not enough in stock");
		}

		orderDetail.setNameVn(product.getNameVn());
		orderDetail.setNameEng(product.getNameEng());
		orderDetail.setPrice(product.getPrice());
		orderDetail.setSubtotal(orderDetail.getPrice().multiply(BigDecimal.valueOf(orderDetail.getQuantity())));
		orderDetail.setProduct(product);
	}
}
