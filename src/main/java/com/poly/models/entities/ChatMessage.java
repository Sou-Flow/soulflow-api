package com.poly.models.entities;

import lombok.Data;

@Data
public class ChatMessage {
    private String sender;
    private String photo;
    private String content;
}
