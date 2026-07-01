package com.poly.models.mappers;

import java.time.LocalDateTime;
import java.util.List;

import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.springframework.beans.factory.annotation.Autowired;

import com.poly.models.entities.Discount;
import com.poly.models.repositories.DiscountRepository;
import com.poly.models.requests.DiscountRequest;
import com.poly.models.responses.DiscountResponse;
import com.poly.utils.LocaleUtil;

import jakarta.persistence.EntityNotFoundException;

@Mapper(componentModel = "spring", uses = {ProductMapper.class}) 
public abstract class DiscountMapper {
    
    @Autowired
    protected LocaleUtil localeHelper;

    @Autowired
    protected DiscountRepository discountRepo;

    @Mapping(target = "code",               ignore = true)
    @Mapping(target = "expired",            ignore = true)
    @Mapping(target = "createdDate",        ignore = true) 
    @Mapping(target = "deleted",          	ignore = true)
    @Mapping(target = "products",           source = "productRequests")
    public abstract Discount toEntity(DiscountRequest request); 

    @Mapping(target = "createdDate", 	    source = "createdDate",             dateFormat = "dd-MM-yyyy HH:mm:ss")
    @Mapping(target = "expiredDate", 	    source = "expiredDate",             dateFormat = "dd-MM-yyyy HH:mm:ss")
	@Mapping(target = "percentage", 		source = "percentage",              numberFormat = "#.##")
    @Mapping(target = "description",        expression = "java(localeHelper.getDescription(discount.getDescriptionVn(), discount.getDescriptionEng()))")
    @Mapping(target = "productResponses",   source = "products",                qualifiedByName = "basicResponse")
    public abstract DiscountResponse toResponse(Discount discount);

    public abstract List<Discount> toEntityList(List<DiscountRequest> discountRequests);

    public abstract List<DiscountResponse> toResponseList(List<Discount> discounts);
    
    @AfterMapping
    protected void afterToEntity(DiscountRequest request, @MappingTarget Discount discount) {

        Long pk = request.getPk();
        LocalDateTime now = LocalDateTime.now();
        
        if (pk != null) {
            Discount oldDiscount = discountRepo.findById(Long.valueOf(pk))
                .orElseThrow(() -> new EntityNotFoundException("Discount not found with pk: " + pk));
            
            if (discount.getExpiredDate() != null) {
            	if (discount.getExpiredDate().isAfter(now)) {
                    discount.setExpired(true);
                } else {
                    discount.setExpired(false);
                }
    		} else {
    			discount.setExpiredDate(oldDiscount.getExpiredDate());
    			discount.setExpired(oldDiscount.getExpired());
    		}
  
            discount.setCode(oldDiscount.getCode());
            discount.setCreatedDate(oldDiscount.getCreatedDate());
            discount.setDeleted(oldDiscount.getDeleted());
            return;
        }
        
        if (discount.getExpiredDate() != null) {
        	if (discount.getExpiredDate().isAfter(now)) {
                discount.setExpired(true);
            } else {
                discount.setExpired(false);
            }
		} else {
			discount.setExpiredDate(LocalDateTime.now());
			discount.setExpired(false);
		}
      
        discount.setCode("D-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        discount.setCreatedDate(now); 
        discount.setDeleted(false);
    }
}
