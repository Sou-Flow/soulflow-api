package com.poly.models.requests;

import lombok.Data;

@Data
public class VerifyOtpRequest {
    private String email;
    private String otp;
}
