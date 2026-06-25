package com.souflow;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SouflowApplication {

  public static void main(String[] args) {
    try {
      Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
      dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    } catch (Exception e) {
      System.out.println(
          "No .env file found or error loading it. Proceeding with default environment variables.");
    }

    SpringApplication.run(SouflowApplication.class, args);
  }
}
