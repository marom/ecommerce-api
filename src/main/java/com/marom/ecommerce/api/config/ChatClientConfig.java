package com.marom.ecommerce.api.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Builds the shared {@link ChatClient} from the auto-configured {@code ChatClient.Builder}
 * (which wraps the Ollama {@code ChatModel} selected in {@code application.properties}).
 */
@Configuration
public class ChatClientConfig {

    @Bean
    ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
