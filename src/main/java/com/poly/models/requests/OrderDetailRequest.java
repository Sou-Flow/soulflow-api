package com.poly.models.requests;

import lombok.Data;

@Data
public class OrderDetailRequest {
    private Long pk;
    private Integer quantity;
    private Long productPk;
}
