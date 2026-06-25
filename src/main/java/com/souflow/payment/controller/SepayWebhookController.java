package com.souflow.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.souflow.order.entity.Order;
import com.souflow.order.repository.OrderRepository;
import com.souflow.payment.dto.SepayWebhookRequest;
import com.souflow.payment.entity.Payment;
import com.souflow.payment.repository.PaymentRepository;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class SepayWebhookController {

  @Value("${sepay.webhook.secret}")
  private String secretKey;

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;
  private final ObjectMapper objectMapper;

  @PostMapping("/sepay-webhook")
  public ResponseEntity<?> handleSepayWebhook(
      @RequestHeader(value = "X-SePay-Signature", required = false) String sepaySignature,
      @RequestHeader(value = "X-SePay-Timestamp", required = false) String sepayTimestamp,
      @RequestBody byte[] rawPayloadBytes) {

    if (sepaySignature == null
        || sepaySignature.isEmpty()
        || sepayTimestamp == null
        || sepayTimestamp.isEmpty()) {
      log.warn("Missing X-SePay-Signature or X-SePay-Timestamp header!");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    try {
      String actualSecretKey = getSecretKey();
      String generatedSignature =
          generateHmacSha256(sepayTimestamp, rawPayloadBytes, actualSecretKey);

      String expectedSignature =
          sepaySignature.startsWith("sha256=") ? sepaySignature.substring(7) : sepaySignature;

      if (!generatedSignature.equalsIgnoreCase(expectedSignature)) {
        log.warn(
            "Invalid SePay Signature! Generated: {}, Expected: {}",
            generatedSignature,
            expectedSignature);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
      }

      String rawPayload = new String(rawPayloadBytes, StandardCharsets.UTF_8);
      SepayWebhookRequest payload = objectMapper.readValue(rawPayload, SepayWebhookRequest.class);
      log.info("Received SePay Webhook Payload: {}", payload);

      log.info("Looking for order with code/id: {}", payload.getCode());
      Order order = orderRepository.findByBusinessIdAndDeletedFalse(payload.getCode()).orElse(null);

      if (order == null) {
        log.warn("Order not found with code: {}", payload.getCode());
        return ResponseEntity.ok(Map.of("success", true));
      }

      log.info("Checking duplication for SePay transaction ID: {}", payload.getId());
      if ("PAID".equals(order.getStatus())) {
        log.warn(
            "Transaction {} already processed (Order {} is already PAID)",
            payload.getId(),
            payload.getCode());
        return ResponseEntity.ok(Map.of("success", true));
      }

      log.info(
          "Comparing amounts - Order Total: {}, Transferred: {}",
          order.getTotal(),
          payload.getTransferAmount());

      BigDecimal transferAmount = BigDecimal.valueOf(payload.getTransferAmount());
      if ("WAITING_PAYMENT".equals(order.getStatus())
          && transferAmount.compareTo(order.getTotal()) >= 0) {
        order.setStatus("PAID");
        orderRepository.save(order);

        Payment payment =
            Payment.builder()
                .amount(transferAmount)
                .paymentDate(LocalDateTime.now())
                .order(order)
                .build();
        paymentRepository.save(payment);

        log.info(
            "Order {} marked as PAID via SePay Transaction {}",
            order.getBusinessId(),
            payload.getId());
      } else {
        log.warn(
            "Failed to process payment. Order {} status is {} or transfer amount {} is less than total {}",
            order.getBusinessId(),
            order.getStatus(),
            transferAmount,
            order.getTotal());
      }

    } catch (Exception e) {
      log.error("Error processing SePay webhook", e);
    }

    return ResponseEntity.ok(Map.of("success", true));
  }

  private String getSecretKey() {
    if (this.secretKey != null && !this.secretKey.trim().isEmpty()) {
      return this.secretKey;
    }
    // Fallback: Read manually from absolute path .env
    try {
      java.util.Properties props = new java.util.Properties();
      props.load(new java.io.FileInputStream("d:/soulflow/soulflow-api/.env"));
      String key = props.getProperty("SEPAY_SECRET_KEY");
      if (key != null && !key.trim().isEmpty()) {
        return key;
      }
    } catch (Exception e) {
      log.warn("Could not read .env file for fallback SEPAY_SECRET_KEY: " + e.getMessage());
    }
    return null;
  }

  private String generateHmacSha256(String timestamp, byte[] data, String key) {
    if (key == null || key.trim().isEmpty()) {
      log.error("SEPAY_SECRET_KEY is not configured or empty! Cannot verify signature.");
      return "";
    }
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      SecretKeySpec secretKeySpec =
          new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
      mac.init(secretKeySpec);

      String prefix = timestamp + ".";
      mac.update(prefix.getBytes(StandardCharsets.UTF_8));
      byte[] hashBytes = mac.doFinal(data);

      StringBuilder hexString = new StringBuilder();
      for (byte b : hashBytes) {
        String hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) {
          hexString.append('0');
        }
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (Exception e) {
      log.error("Error generating HMAC-SHA256 signature", e);
      return "";
    }
  }
}
