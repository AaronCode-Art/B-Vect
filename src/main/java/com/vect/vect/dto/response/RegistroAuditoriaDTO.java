package com.vect.vect.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.UUID;

public record RegistroAuditoriaDTO(
    Long id,
    UUID usuarioId,
    String accion,
    String tipoEntidad,
    UUID entidadId,
    JsonNode valoresAnteriores,
    JsonNode valoresNuevos,
    String direccionIp,
    String agenteUsuario,
    LocalDateTime creadoEn
) {}
