package com.vect.vect.dto.response;

import com.vect.vect.entity.Evidencia;

import java.time.LocalDateTime;
import java.util.UUID;

public record EvidenciaDTO(
    UUID id,
    UUID incidenciaId,
    UUID subidoPorId,
    String subidoPorNombre,
    String nombreArchivo,
    String tipoMime,
    Long tamanoBytes,
    String url,
    String descripcion,
    LocalDateTime creadoEn
) {}
