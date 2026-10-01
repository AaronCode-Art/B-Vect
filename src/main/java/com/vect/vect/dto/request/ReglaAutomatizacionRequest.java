package com.vect.vect.dto.request;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReglaAutomatizacionRequest(
    @NotBlank @Size(max = 180) String nombre,
    String descripcion,
    @NotBlank @Size(max = 100) String nombreEvento,
    JsonNode condiciones,
    JsonNode acciones,
    @Min(0) Integer prioridad,
    Boolean activo
) {}
