package com.vect.vect.service;

import com.vect.vect.dto.response.MensajeChatDTO;
import com.vect.vect.dto.response.MensajeConversacionDTO;
import org.springframework.beans.factory.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Component
public class ChatMessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(ChatMessagePublisher.class);
    private final ObjectProvider<ChatWebSocketHandler> chatWebSocketHandlerProvider;

    public ChatMessagePublisher(ObjectProvider<ChatWebSocketHandler> chatWebSocketHandlerProvider) {
        this.chatWebSocketHandlerProvider = chatWebSocketHandlerProvider;
    }

    public void publishConversation(UUID conversationId, MensajeConversacionDTO message) {
        publishAfterCommit("/topic/conversations/" + conversationId, message);
    }

    public void publishIncident(UUID incidentId, String channel, MensajeChatDTO message) {
        publishAfterCommit("/topic/incidencias/" + incidentId + "/chat/" + channel, message);
    }

    public void publishDataUpdate() {
        publishAfterCommit("/topic/updates", java.util.Map.of("type", "DATA_UPDATED"));
    }

    private void publishAfterCommit(String destination, Object message) {
        Runnable publish = () -> {
            try {
                chatWebSocketHandlerProvider.getObject().publish(destination, message);
            } catch (RuntimeException exception) {
                log.error("No se pudo publicar el mensaje de chat en {}", destination, exception);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publish.run();
                }
            });
        } else {
            publish.run();
        }
    }
}
