package com.souflow.common.util;

import java.util.UUID;

public final class IdGenerator {

  private IdGenerator() {}

  public static String generateBusinessId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }

  public static String generateOrderId() {
    java.time.format.DateTimeFormatter formatter =
        java.time.format.DateTimeFormatter.ofPattern("yyMMdd");
    String datePart = java.time.LocalDate.now().format(formatter);
    int randomThreeDigit = 100 + new java.util.Random().nextInt(900); // 100 to 999
    return "SF" + datePart + randomThreeDigit;
  }
}
