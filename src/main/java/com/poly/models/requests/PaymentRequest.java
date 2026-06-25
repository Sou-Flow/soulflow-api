package com.poly.models.requests;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentRequest {

    private Long pk;

    private BigDecimal amount;

    private Long orderPk;
}
