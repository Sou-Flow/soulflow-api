package com.poly.models.services;

import com.poly.models.requests.PaymentRequest;
import com.poly.models.responses.PaymentResponse;
import com.poly.models.requests.SepayWebhookRequest;

public interface PaymentService {

    PaymentResponse save(PaymentRequest request);
    void processSepayWebhook(String signature, String timestamp, byte[] rawPayloadBytes);
}
