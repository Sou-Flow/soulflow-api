package com.souflow.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class SepayWebhookRequest {
  private Integer id;
  private String code;
  private Integer transferAmount;
  private String content;
}
