package com.vect.vect.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.entity.MensajeIncidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.ConcurrentWebSocketSessionDecorator;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(ChatWebSocketHandler.class);
    private static final Pattern CONVERSATION_DESTINATION =
        Pattern.compile("^/topic/conversations/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12})$");
    private static final Pattern INCIDENT_DESTINATION =
        Pattern.compile("^/topic/incidencias/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-"
            + "[0-9a-fA-F]{4}-[0-9a-fA-F]{12})/chat/([A-Z_]+)$");
    private static final String UPDATES_DESTINATION = "/topic/updates";

    private final JwtService jwtService;
    private final UsuarioRepository usuarioRepository;
    private final ConversacionService conversacionService;
    private final MensajeIncidenciaService mensajeIncidenciaService;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, ClientConnection> connections = new ConcurrentHashMap<>();

    public ChatWebSocketHandler(JwtService jwtService, UsuarioRepository usuarioRepository,
                                ConversacionService conversacionService,
                                MensajeIncidenciaService mensajeIncidenciaService,
                                ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.usuarioRepository = usuarioRepository;
        this.conversacionService = conversacionService;
        this.mensajeIncidenciaService = mensajeIncidenciaService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        WebSocketSession safeSession = new ConcurrentWebSocketSessionDecorator(session, 10_000, 512 * 1024);
        connections.put(session.getId(), new ClientConnection(safeSession));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws IOException {
        ClientConnection connection = connections.get(session.getId());
        if (connection == null) {
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }

        JsonNode frame;
        try {
            frame = objectMapper.readTree(message.getPayload());
        } catch (JsonProcessingException exception) {
            sendError(connection, "El mensaje WebSocket no contiene JSON válido.");
            return;
        }
        if (frame == null || !frame.isObject() || !frame.path("type").isTextual()) {
            sendError(connection, "El tipo de mensaje WebSocket no es válido.");
            return;
        }

        String type = frame.path("type").textValue();
        if ("AUTHENTICATE".equals(type)) {
            authenticate(connection, frame.path("token").asText(null));
        } else if ("SUBSCRIBE".equals(type)) {
            subscribe(connection, frame.path("destination").asText(null));
        } else {
            sendError(connection, "Solo se permiten mensajes AUTHENTICATE y SUBSCRIBE.");
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        connections.remove(session.getId());
    }

    public void publish(String destination, Object payload) {
        ObjectNode frame = objectMapper.createObjectNode();
        frame.put("type", "MESSAGE");
        frame.put("destination", destination);
        frame.set("payload", objectMapper.valueToTree(payload));

        connections.values().stream()
            .filter(connection -> connection.subscriptions.contains(destination))
            .forEach(connection -> send(connection, frame));
    }

    private void authenticate(ClientConnection connection, String token) throws IOException {
        if (connection.user != null) {
            sendError(connection, "La conexión ya está autenticada.");
            return;
        }
        if (token == null || token.isBlank()) {
            rejectAuthentication(connection, "Se requiere un token de acceso para conectar el chat.");
            return;
        }

        try {
            var claims = jwtService.parseAccessToken(token);
            UUID userId = jwtService.getUserId(claims);
            connection.user = usuarioRepository.findById(userId)
                .filter(candidate -> Boolean.TRUE.equals(candidate.getActivo()))
                .orElse(null);
        } catch (JwtException | IllegalArgumentException exception) {
            rejectAuthentication(connection, "El token de acceso no es válido.");
            return;
        }

        if (connection.user == null) {
            rejectAuthentication(connection, "La cuenta no está disponible.");
            return;
        }

        ObjectNode frame = objectMapper.createObjectNode();
        frame.put("type", "AUTHENTICATED");
        send(connection, frame);
    }

    private void subscribe(ClientConnection connection, String destination) throws IOException {
        if (connection.user == null) {
            sendError(connection, "Autentica la conexión antes de suscribirte.");
            return;
        }
        if (destination == null) {
            sendError(connection, "Destino de suscripción no válido.");
            return;
        }

        try {
            Matcher conversation = CONVERSATION_DESTINATION.matcher(destination);
            if (conversation.matches()) {
                conversacionService.verificarAccesoChat(UUID.fromString(conversation.group(1)), connection.user);
            } else if (UPDATES_DESTINATION.equals(destination)) {
                // This channel carries only a generic invalidation signal, never record data.
            } else {
                Matcher incident = INCIDENT_DESTINATION.matcher(destination);
                if (!incident.matches()) {
                    sendError(connection, "No está permitido suscribirse a este destino.");
                    return;
                }
                MensajeIncidencia.CanalMensaje canal;
                try {
                    canal = MensajeIncidencia.CanalMensaje.valueOf(incident.group(2));
                } catch (IllegalArgumentException exception) {
                    sendError(connection, "Canal de chat no válido.");
                    return;
                }
                mensajeIncidenciaService.verificarAccesoChat(
                    UUID.fromString(incident.group(1)), canal, connection.user);
            }
        } catch (ApiException exception) {
            sendError(connection, exception.getMessage());
            return;
        }

        connection.subscriptions.add(destination);
        ObjectNode frame = objectMapper.createObjectNode();
        frame.put("type", "SUBSCRIBED");
        frame.put("destination", destination);
        send(connection, frame);
    }

    private void rejectAuthentication(ClientConnection connection, String message) throws IOException {
        sendError(connection, message);
        connection.session.close(CloseStatus.POLICY_VIOLATION);
    }

    private void sendError(ClientConnection connection, String message) throws IOException {
        ObjectNode frame = objectMapper.createObjectNode();
        frame.put("type", "ERROR");
        frame.put("message", message);
        send(connection, frame);
    }

    private void send(ClientConnection connection, JsonNode frame) {
        try {
            connection.session.sendMessage(new TextMessage(objectMapper.writeValueAsString(frame)));
        } catch (IOException exception) {
            log.warn("No se pudo enviar un evento por WebSocket a la conexión {}",
                connection.session.getId(), exception);
            try {
                connection.session.close(CloseStatus.SERVER_ERROR);
            } catch (IOException closeException) {
                log.warn("No se pudo cerrar la conexión WebSocket {}", connection.session.getId(), closeException);
            }
            connections.remove(connection.session.getId());
        }
    }

    private static final class ClientConnection {
        private final WebSocketSession session;
        private final Set<String> subscriptions = ConcurrentHashMap.newKeySet();
        private volatile Usuario user;

        private ClientConnection(WebSocketSession session) {
            this.session = session;
        }
    }
}
