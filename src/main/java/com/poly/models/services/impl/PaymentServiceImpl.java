package com.poly.models.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.poly.models.entities.Payment;
import com.poly.models.mappers.PaymentMapper;
import com.poly.models.repositories.PaymentRepository;
import com.poly.models.requests.PaymentRequest;
import com.poly.models.responses.PaymentResponse;
import com.poly.models.services.PaymentService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {
    
    private final PaymentRepository paymentRepo;
    
    private final PaymentMapper paymentMapper;

    @Override
    @Transactional
    public PaymentResponse save(PaymentRequest request) {
        Payment payment = paymentMapper.toEntity(request);
        Payment saved = paymentRepo.save(payment);
        return paymentMapper.toResponse(saved);
    }
}
