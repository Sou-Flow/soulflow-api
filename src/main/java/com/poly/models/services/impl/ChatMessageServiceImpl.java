package com.poly.models.services.impl;

import java.util.concurrent.CompletableFuture;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.poly.models.entities.ChatMessage;
import com.poly.models.mappers.ChatMessageMapper;
import com.poly.models.repositories.ChatMessageRepository;
import com.poly.models.requests.ChatMessageRequest;
import com.poly.models.responses.ChatMessageResponse;
import com.poly.models.services.ChatMessageService;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    public final ChatMessageMapper chatMessageMapper;

    public final ChatMessageRepository chatMessageRepo;

    public CompletableFuture<ChatMessageResponse> processMessage(ChatMessageRequest request) {
        // your existing chat logic — save message, call LLM, etc.
        ChatMessageResponse response = save(request);
        return CompletableFuture.completedFuture(response);
    }

    @Async("chatTaskExecutor")
    public ChatMessageResponse save(ChatMessageRequest request) {
        ChatMessage message = chatMessageMapper.toEntity(request);
        return chatMessageMapper.toResponse(chatMessageRepo.save(message));
    }
}
