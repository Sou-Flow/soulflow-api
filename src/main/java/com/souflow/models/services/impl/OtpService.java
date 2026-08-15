package com.souflow.models.services.impl;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souflow.models.requests.AccountRequest;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OTP_PREFIX = "OTP_FORGOT_PASSWORD:";
    private static final String OTP_REGISTER_PREFIX = "OTP_REGISTER:";
    private static final String OTP_REGISTER_DATA_PREFIX = "OTP_REGISTER_DATA:";
    private static final long OTP_VALIDITY_MINUTES = 5;

    public String generateOtp(String email) {
        // Generate a 6-digit random OTP
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        // Save to Redis with expiration
        redisTemplate.opsForValue().set(
                OTP_PREFIX + email,
                otp,
                OTP_VALIDITY_MINUTES,
                TimeUnit.MINUTES
        );
        
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            // Valid OTP, we should delete it so it cannot be reused
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }

    public boolean verifyOtpWithoutDeleting(String email, String otp) {
        String key = OTP_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        return storedOtp != null && storedOtp.equals(otp);
    }

    public String generateRegisterOtp(String email, AccountRequest request) {
        SecureRandom random = new SecureRandom();
        int otpValue = 100000 + random.nextInt(900000);
        String otp = String.valueOf(otpValue);
        
        // Save OTP to Redis
        redisTemplate.opsForValue().set(
                OTP_REGISTER_PREFIX + email,
                otp,
                OTP_VALIDITY_MINUTES,
                TimeUnit.MINUTES
        );

        // Save Registration Request Data to Redis
        try {
            String json = objectMapper.writeValueAsString(request);
            redisTemplate.opsForValue().set(
                    OTP_REGISTER_DATA_PREFIX + email,
                    json,
                    OTP_VALIDITY_MINUTES,
                    TimeUnit.MINUTES
            );
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lưu dữ liệu đăng ký vào cache", e);
        }
        
        return otp;
    }

    public boolean validateRegisterOtp(String email, String otp) {
        String key = OTP_REGISTER_PREFIX + email;
        String storedOtp = redisTemplate.opsForValue().get(key);
        
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }

    public AccountRequest getRegisterData(String email) {
        String key = OTP_REGISTER_DATA_PREFIX + email;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, AccountRequest.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi đọc dữ liệu đăng ký từ cache", e);
        }
    }

    public void deleteRegisterData(String email) {
        redisTemplate.delete(OTP_REGISTER_DATA_PREFIX + email);
    }
}
