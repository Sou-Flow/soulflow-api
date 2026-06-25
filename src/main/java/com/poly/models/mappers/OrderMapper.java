package com.poly.models.mappers;
import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.poly.models.entities.Account;
import com.poly.models.entities.Order;
import com.poly.models.entities.OrderDetail;
import com.poly.models.repositories.OrderRepository;
import com.poly.models.requests.OrderRequest;
import com.poly.models.responses.OrderResponse;

import jakarta.persistence.EntityNotFoundException;


@Component
@Mapper(componentModel = "spring", uses = {OrderDetailMapper.class})
public abstract class OrderMapper {
	
	@Autowired
	OrderRepository orderRepo;

	@Mapping(target = "code", 		 			ignore = true)
	@Mapping(target = "total", 		 			ignore = true)
	@Mapping(target = "createdDate", 			ignore = true)
	@Mapping(target = "expiredDate", 			ignore = true)
	@Mapping(target = "expired", 				ignore = true)
	@Mapping(target = "account", 	 			ignore = true)
	@Mapping(target = "deleted", 	 			ignore = true)
	@Mapping(target = "orderDetails", 			source = "request.orderDetailRequests")
	public abstract Order toEntity(OrderRequest request);
	
	@Mapping(target = "createdDate", 	source = "createdDate", dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "expiredDate", 	source = "expiredDate", dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "total", 			source = "total", numberFormat = "#.##")
	@Mapping(target = "accountPk",			  	source = "account.pk")
	@Mapping(target = "orderDetailResponses", 	source = "orderDetails")
	public abstract OrderResponse toResponse(Order order);
	
	public abstract List<OrderResponse> toResponseList(List<Order> orders);
	
	@AfterMapping
	protected void afterToEntity(OrderRequest request, @MappingTarget Order order) {
		
		Long pk = order.getPk();
		if (pk != null) {
			Order oldOrder = orderRepo.findById(pk)
				.orElseThrow(() -> new EntityNotFoundException("Order not found with pk: "+ pk));
			order.setCode(oldOrder.getCode());
			order.setExpiredDate(oldOrder.getExpiredDate());
			order.setExpired(oldOrder.getExpired());
			order.setCreatedDate(oldOrder.getCreatedDate());
			order.setAccount(oldOrder.getAccount());
			order.calTotal();
			order.setDeleted(order.getDeleted());
			return;
		}
		order.setCode("O" + String.format("%06d", orderRepo.count() + 1));
		order.setCreatedDate(LocalDateTime.now());
		order.setExpiredDate(LocalDateTime.now().plusWeeks(2));
		order.setExpired(false);
		Account account = new Account();
		account.setPk(request.getAccountPk());
		order.setAccount(account);
		for (OrderDetail od : order.getOrderDetails()) {
			od.setOrder(order);
		}
		order.calTotal();
		order.setDeleted(false);		
	}
}
