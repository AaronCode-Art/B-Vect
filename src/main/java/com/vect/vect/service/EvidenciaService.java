package com.vect.vect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.EvidenciaDTO;
import com.vect.vect.entity.Evidencia;
import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.EvidenciaMapper;
import com.vect.vect.repository.EvidenciaRepository;
import com.vect.vect.repository.IncidenciaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EvidenciaService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EvidenciaService.class);
    private static final long MAX_FILE_BYTES = 8L * 1024 * 1024;

    private final EvidenciaRepository evidenciaRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final Cloudinary cloudinary;
    private final DatabaseActorContext databaseActorContext;
    private final EvidenciaMapper evidenciaMapper;
    private final String cloudinaryFolder;
    private final boolean cloudinaryConfigured;

    public EvidenciaService(EvidenciaRepository evidenciaRepository, IncidenciaRepository incidenciaRepository,
                            Cloudinary cloudinary, DatabaseActorContext databaseActorContext,
                            EvidenciaMapper evidenciaMapper,
                            @Value("${app.cloudinary.folder:vect/evidencias}") String cloudinaryFolder,
                            @Value("${app.cloudinary.cloud-name:}") String cloudName,
                            @Value("${app.cloudinary.api-key:}") String apiKey,
                            @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this.evidenciaRepository = evidenciaRepository;
        this.incidenciaRepository = incidenciaRepository;
        this.cloudinary = cloudinary;
        this.databaseActorContext = databaseActorContext;
        this.evidenciaMapper = evidenciaMapper;
        this.cloudinaryFolder = cloudinaryFolder;
        this.cloudinaryConfigured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    @Transactional(readOnly = true)
    public List<EvidenciaDTO> listar(UUID incidenciaId, Usuario actor) {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        verificarVisibilidad(incidencia, actor);
        return evidenciaRepository.findAllByIncidencia_IdOrderByCreadoEnDesc(incidenciaId).stream()
            .filter(evidencia -> actor.getRol() != Usuario.RolUsuario.EMPLEADO
                || evidencia.getSubidoPor().getId().equals(actor.getId()))
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<EvidenciaDTO> listarTodas(Usuario actor) {
        if (actor.getRol() != Usuario.RolUsuario.ADMIN
                && actor.getRol() != Usuario.RolUsuario.GERENCIA
                && actor.getRol() != Usuario.RolUsuario.SUPERVISOR) {
            throw ApiException.forbidden("ACCESO_DENEGADO", "No tiene permiso para consultar todas las evidencias");
        }
        return evidenciaRepository.findAllByOrderByCreadoEnDesc().stream().map(this::toDto).toList();
    }

    @Transactional
    public EvidenciaDTO subir(UUID incidenciaId, MultipartFile archivo, Usuario subidoPor) throws IOException {
        Incidencia incidencia = buscarIncidencia(incidenciaId);
        verificarVisibilidad(incidencia, subidoPor);

        if (incidencia.getEstado() == Incidencia.EstadoIncidencia.CERRADA) {
            throw ApiException.conflict("INCIDENCIA_CERRADA", "No se pueden subir evidencias a una incidencia cerrada");
        }

        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.toLowerCase(java.util.Locale.ROOT).startsWith("image/")) {
            throw ApiException.badRequest("ARCHIVO_NO_ES_IMAGEN", "El archivo debe ser una imagen");
        }

        if (archivo.isEmpty() || archivo.getSize() > MAX_FILE_BYTES) {
            throw ApiException.badRequest("TAMANO_ARCHIVO_INVALIDO",
                "La imagen debe tener contenido y no superar los 8 MB");
        }
        if (!cloudinaryConfigured || cloudinaryFolder == null || cloudinaryFolder.isBlank()) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ALMACENAMIENTO_NO_CONFIGURADO",
                "El almacenamiento de evidencias no está configurado");
        }

        Map<?, ?> uploadResult;
        try {
            uploadResult = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                "folder", cloudinaryFolder,
                "resource_type", "image",
                "use_filename", true,
                "unique_filename", true));
        } catch (Exception exception) {
            log.error("Falló la carga de evidencia para la incidencia {}", incidenciaId, exception);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "EVIDENCIA_NO_SUBIDA",
                "No se pudo almacenar la imagen; verifique la configuración del servicio");
        }
        Object publicId = uploadResult.get("public_id");
        if (!(publicId instanceof String storageKey) || storageKey.isBlank()) {
            log.error("Cloudinary no devolvió una clave de almacenamiento para incidencia {}", incidenciaId);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "EVIDENCIA_NO_SUBIDA",
                "El proveedor de almacenamiento no devolvió una clave válida");
        }

        try {
            Evidencia evidencia = new Evidencia();
            evidencia.setIncidencia(incidencia);
            evidencia.setSubidoPor(subidoPor);
            evidencia.setNombreArchivo(nombreArchivo(archivo.getOriginalFilename()));
            evidencia.setClaveAlmacenamiento(storageKey);
            evidencia.setTipoMime(contentType);
            evidencia.setTamanoBytes(archivo.getSize());
            databaseActorContext.setActor(subidoPor);
            return toDto(evidenciaRepository.save(evidencia));
        } catch (RuntimeException exception) {
            try {
                cloudinary.uploader().destroy(storageKey, ObjectUtils.asMap("resource_type", "image"));
            } catch (Exception cleanupException) {
                log.warn("No se pudo eliminar la evidencia remota {} tras fallar su registro",
                    storageKey, cleanupException);
            }
            throw exception;
        }
    }

    private EvidenciaDTO toDto(Evidencia evidencia) {
        String url = cloudinary.url().secure(true).resourceType("image")
            .generate(evidencia.getClaveAlmacenamiento());
        return evidenciaMapper.toDto(evidencia, url);
    }

    private Incidencia buscarIncidencia(UUID id) {
        return incidenciaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));
    }

    private void verificarVisibilidad(Incidencia incidencia, Usuario actor) {
        boolean permitido = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> true;
            case TECNICO -> incidencia.getTecnicoAsignado() != null
                && incidencia.getTecnicoAsignado().getId().equals(actor.getId());
            case EMPLEADO -> incidencia.getReportante().getId().equals(actor.getId());
        };
        if (!permitido) {
            throw ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada");
        }
    }

    private String nombreArchivo(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "evidencia";
        }
        String nombre = nombreOriginal.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).trim();
        if (nombre.isBlank()) {
            return "evidencia";
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }
}
