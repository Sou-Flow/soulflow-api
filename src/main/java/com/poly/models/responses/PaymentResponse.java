package com.poly.models.responses;

import lombok.Data;

@Data
public class PaymentResponse {

    private String pk;

    private String paid;

    private String amount;

    private String paymentDate;

    private OrderResponse orderResponse;
}
