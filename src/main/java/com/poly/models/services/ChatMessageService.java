package com.poly.models.services;

import java.util.concurrent.CompletableFuture;

import com.poly.models.requests.ChatMessageRequest;
import com.poly.models.responses.ChatMessageResponse;

public interface ChatMessageService {
    
    public CompletableFuture<ChatMessageResponse> processMessage(ChatMessageRequest request);
}
