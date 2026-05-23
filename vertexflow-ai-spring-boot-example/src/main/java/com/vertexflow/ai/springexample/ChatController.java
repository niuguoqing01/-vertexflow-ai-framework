package com.vertexflow.ai.springexample;

import com.vertexflow.ai.core.chat.AiClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final AiClient aiClient;

    public ChatController(AiClient aiClient) {
        this.aiClient = aiClient;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) {
        return aiClient.chat(message);
    }
}