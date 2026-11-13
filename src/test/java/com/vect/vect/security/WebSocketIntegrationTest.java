package com.vect.vect.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.config.ChatJsonConfig;
import com.vect.vect.config.WebSocketConfig;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.service.ChatWebSocketHandler;
import com.vect.vect.service.ConversacionService;
import com.vect.vect.service.MensajeIncidenciaService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernateJpaAutoConfiguration;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest(
    classes = WebSocketIntegrationTest.TestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "app.cors.allowed-origins=http://localhost")
class WebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private ConversacionService conversacionService;

    @Autowired
    private ChatWebSocketHandler chatWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private StandardWebSocketClient webSocketClient;
    private WebSocketSession session;

    @AfterEach
    void disconnect() throws Exception {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    void authenticatesSubscribesAndReceivesMessagesOverNativeWebSocket() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID conversationId = UUID.randomUUID();
        Usuario user = new Usuario();
        user.setId(userId);
        user.setRol(Usuario.RolUsuario.EMPLEADO);
        user.setActivo(true);

        Claims claims = mock(Claims.class);
        when(jwtService.parseAccessToken("test-token")).thenReturn(claims);
        when(jwtService.getUserId(claims)).thenReturn(userId);
        when(usuarioRepository.findById(userId)).thenReturn(Optional.of(user));

        LinkedBlockingQueue<JsonNode> received = new LinkedBlockingQueue<>();
        WebSocketHandler clientHandler = new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
                received.offer(objectMapper.readTree(message.getPayload()));
            }
        };
        webSocketClient = new StandardWebSocketClient();
        session = webSocketClient.execute(clientHandler, new WebSocketHttpHeaders(),
            URI.create("ws://localhost:" + port + "/ws")).get(5, TimeUnit.SECONDS);

        String destination = "/topic/conversations/" + conversationId;
        session.sendMessage(new TextMessage(objectMapper.createObjectNode()
            .put("type", "SUBSCRIBE")
            .put("destination", destination)
            .toString()));
        assertThat(nextFrame(received).path("type").asText()).isEqualTo("ERROR");

        session.sendMessage(new TextMessage("""
            {"type":"AUTHENTICATE","token":"test-token"}
            """));
        assertThat(nextFrame(received).path("type").asText()).isEqualTo("AUTHENTICATED");

        session.sendMessage(new TextMessage(objectMapper.createObjectNode()
            .put("type", "SUBSCRIBE")
            .put("destination", destination)
            .toString()));
        assertThat(nextFrame(received).path("type").asText()).isEqualTo("SUBSCRIBED");
        verify(conversacionService, timeout(2000)).verificarAccesoChat(conversationId, user);

        session.sendMessage(new TextMessage(objectMapper.createObjectNode()
            .put("type", "SUBSCRIBE")
            .put("destination", "/topic/updates")
            .toString()));
        assertThat(nextFrame(received).path("type").asText()).isEqualTo("SUBSCRIBED");
        chatWebSocketHandler.publish("/topic/updates", Map.of("type", "DATA_UPDATED"));
        JsonNode update = nextFrame(received);
        assertThat(update.path("destination").asText()).isEqualTo("/topic/updates");
        assertThat(update.path("payload").path("type").asText()).isEqualTo("DATA_UPDATED");

        chatWebSocketHandler.publish(destination, Map.of(
            "text", "hola",
            "creadoEn", LocalDateTime.of(2026, 1, 2, 3, 4, 5)));
        JsonNode event = nextFrame(received);
        assertThat(event.path("type").asText()).isEqualTo("MESSAGE");
        assertThat(event.path("destination").asText()).isEqualTo(destination);
        assertThat(event.path("payload").path("text").asText()).isEqualTo("hola");
        assertThat(event.path("payload").path("creadoEn").asText()).isEqualTo("2026-01-02T03:04:05");

        UUID deniedConversationId = UUID.randomUUID();
        doThrow(ApiException.forbidden("ACCESO_DENEGADO", "No pertenece a la conversación"))
            .when(conversacionService).verificarAccesoChat(deniedConversationId, user);
        String deniedDestination = "/topic/conversations/" + deniedConversationId;
        session.sendMessage(new TextMessage(objectMapper.createObjectNode()
            .put("type", "SUBSCRIBE")
            .put("destination", deniedDestination)
            .toString()));
        JsonNode denied = nextFrame(received);
        assertThat(denied.path("type").asText()).isEqualTo("ERROR");
        assertThat(denied.path("message").asText()).isEqualTo("No pertenece a la conversación");
    }

    private JsonNode nextFrame(LinkedBlockingQueue<JsonNode> received) throws InterruptedException {
        JsonNode frame = received.poll(5, TimeUnit.SECONDS);
        assertThat(frame).as("server WebSocket frame").isNotNull();
        return frame;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        UserDetailsServiceAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class
    })
    @Import({ChatJsonConfig.class, WebSocketConfig.class, ChatWebSocketHandler.class})
    static class TestApplication {

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }

        @Bean
        UsuarioRepository usuarioRepository() {
            return mock(UsuarioRepository.class);
        }

        @Bean
        ConversacionService conversacionService() {
            return mock(ConversacionService.class);
        }

        @Bean
        MensajeIncidenciaService mensajeIncidenciaService() {
            return mock(MensajeIncidenciaService.class);
        }

    }
}
