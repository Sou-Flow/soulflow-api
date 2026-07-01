package com.poly.models.services;

import com.poly.models.requests.ShippingFeeRequest;
import com.poly.models.responses.ShippingFeeResponse;

public interface ShippingService {
    ShippingFeeResponse calculateFee(ShippingFeeRequest request);
}
