package com.souflow.common.util;

import java.util.UUID;

public final class IdGenerator {

  private IdGenerator() {}

  public static String generateBusinessId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
  }
}
