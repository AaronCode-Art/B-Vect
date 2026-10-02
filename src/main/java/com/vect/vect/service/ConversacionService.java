package com.vect.vect.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.request.ConversacionCreateRequest;
import com.vect.vect.dto.request.MensajeConversacionRequest;
import com.vect.vect.dto.response.ConversacionDTO;
import com.vect.vect.dto.response.ConversacionParticipanteDTO;
import com.vect.vect.dto.response.MensajeConversacionDTO;
import com.vect.vect.entity.Conversacion;
import com.vect.vect.entity.MensajeConversacion;
import com.vect.vect.entity.MiembroConversacion;
import com.vect.vect.entity.MiembroConversacionId;
import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.ConversacionRepository;
import com.vect.vect.repository.MensajeConversacionRepository;
import com.vect.vect.repository.MiembroConversacionRepository;
import com.vect.vect.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ConversacionService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConversacionService.class);
    private static final long MAX_FILE_BYTES = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
        "jpg", "jpeg", "png", "gif", "webp", "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf",
        "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", "text/plain");

    private final ConversacionRepository conversacionRepository;
    private final MiembroConversacionRepository miembroRepository;
    private final MensajeConversacionRepository mensajeRepository;
    private final UsuarioRepository usuarioRepository;
    private final Cloudinary cloudinary;
    private final boolean cloudinaryConfigured;

    public ConversacionService(ConversacionRepository conversacionRepository,
                               MiembroConversacionRepository miembroRepository,
                               MensajeConversacionRepository mensajeRepository,
                               UsuarioRepository usuarioRepository,
                               Cloudinary cloudinary,
                               @Value("${app.cloudinary.cloud-name:}") String cloudName,
                               @Value("${app.cloudinary.api-key:}") String apiKey,
                               @Value("${app.cloudinary.api-secret:}") String apiSecret) {
        this.conversacionRepository = conversacionRepository;
        this.miembroRepository = miembroRepository;
        this.mensajeRepository = mensajeRepository;
        this.usuarioRepository = usuarioRepository;
        this.cloudinary = cloudinary;
        this.cloudinaryConfigured = !cloudName.isBlank() && !apiKey.isBlank() && !apiSecret.isBlank();
    }

    @Transactional(readOnly = true)
    public List<ConversacionDTO> listar(Usuario actor) {
        return miembroRepository.findAllByUsuario_IdAndSalioEnIsNull(actor.getId()).stream()
            .map(MiembroConversacion::getConversacion)
            .distinct()
            .map(this::toDto)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversacionParticipanteDTO> listarParticipantes(Usuario actor) {
        return usuarioRepository.findAllByActivoTrueOrderByApellidosAscNombresAsc().stream()
            .filter(usuario -> !usuario.getId().equals(actor.getId()))
            .map(usuario -> new ConversacionParticipanteDTO(usuario.getId(),
                usuario.getNombres() + " " + usuario.getApellidos(), usuario.getCorreo(), usuario.getRol().name()))
            .toList();
    }

    @Transactional
    public ConversacionDTO crear(ConversacionCreateRequest request, Usuario actor) {
        List<UUID> participantIds = new ArrayList<>(new HashSet<>(request.participantes()));
        participantIds.remove(actor.getId());
        if (participantIds.size() != request.participantes().size() - (request.participantes().contains(actor.getId()) ? 1 : 0)) {
            throw ApiException.badRequest("PARTICIPANTES_DUPLICADOS", "La lista contiene participantes duplicados");
        }

        if (request.tipo() == Conversacion.TipoConversacion.GRUPAL) {
            if (request.nombre() == null || request.nombre().isBlank()) {
                throw ApiException.badRequest("NOMBRE_GRUPO_REQUERIDO", "Las conversaciones grupales requieren nombre");
            }
            if (participantIds.isEmpty()) {
                throw ApiException.badRequest("PARTICIPANTES_REQUERIDOS", "Agregue al menos un participante");
            }
        } else if (participantIds.size() != 1) {
            throw ApiException.badRequest("CONVERSACION_PRIVADA_INVALIDA",
                "Las conversaciones privadas deben tener exactamente otro participante");
        }

        List<Usuario> participantes = new ArrayList<>();
        participantes.add(actor);
        for (UUID id : participantIds) {
            Usuario usuario = usuarioRepository.findById(id)
                .filter(u -> Boolean.TRUE.equals(u.getActivo()))
                .orElseThrow(() -> ApiException.notFound("PARTICIPANTE_NO_ENCONTRADO",
                    "Uno de los participantes no existe o está inactivo"));
            participantes.add(usuario);
        }

        Conversacion conversacion = new Conversacion();
        conversacion.setTipo(request.tipo());
        conversacion.setNombre(request.tipo() == Conversacion.TipoConversacion.GRUPAL
            ? request.nombre().trim() : null);
        conversacion.setCreadoPor(actor);
        conversacion = conversacionRepository.save(conversacion);

        for (Usuario participante : participantes) {
            MiembroConversacion miembro = new MiembroConversacion();
            miembro.setId(new MiembroConversacionId());
            miembro.setConversacion(conversacion);
            miembro.setUsuario(participante);
            miembroRepository.save(miembro);
        }
        return toDto(conversacion);
    }

    @Transactional(readOnly = true)
    public List<MensajeConversacionDTO> listarMensajes(UUID conversacionId, Usuario actor) {
        verificarMiembro(conversacionId, actor);
        return mensajeRepository.findAllByConversacion_IdAndEliminadoEnIsNullOrderByCreadoEnAsc(conversacionId)
            .stream()
            .map(this::toMensajeDto).toList();
    }

    @Transactional
    public MensajeConversacionDTO enviar(UUID conversacionId, MensajeConversacionRequest request, Usuario actor) {
        Conversacion conversacion = verificarMiembro(conversacionId, actor);
        MensajeConversacion mensaje = new MensajeConversacion();
        mensaje.setConversacion(conversacion);
        mensaje.setRemitente(actor);
        mensaje.setCuerpo(request.cuerpo().trim());
        mensaje = mensajeRepository.save(mensaje);
        conversacion.setActualizadoEn(LocalDateTime.now());
        return toMensajeDto(mensaje);
    }

    @Transactional
    public MensajeConversacionDTO enviarArchivo(UUID conversacionId, MultipartFile archivo, String cuerpo,
                                                Usuario actor)
            throws IOException {
        Conversacion conversacion = verificarMiembro(conversacionId, actor);
        validarArchivo(archivo);
        if (!cloudinaryConfigured) {
            throw new ApiException(HttpStatus.SERVICE_UNAVAILABLE, "ALMACENAMIENTO_NO_CONFIGURADO",
                "El almacenamiento de archivos del chat no está configurado");
        }

        String nombreArchivo = nombreArchivo(archivo.getOriginalFilename());
        String carpeta = conversacion.getTipo() == Conversacion.TipoConversacion.GRUPAL
            ? "vect/chat/grupal" : "vect/chat/personal";
        Map<?, ?> resultado;
        try {
            resultado = cloudinary.uploader().upload(archivo.getBytes(), ObjectUtils.asMap(
                "folder", carpeta,
                "resource_type", "auto",
                "use_filename", true,
                "unique_filename", true));
        } catch (Exception exception) {
            log.error("Falló la carga de un adjunto para la conversación {}", conversacionId, exception);
            throw new ApiException(HttpStatus.BAD_GATEWAY, "ARCHIVO_NO_SUBIDO",
                "No se pudo almacenar el archivo adjunto");
        }

        Object publicId = resultado.get("public_id");
        Object secureUrl = resultado.get("secure_url");
        if (!(publicId instanceof String storageKey) || storageKey.isBlank()
                || !(secureUrl instanceof String url) || url.isBlank()) {
            log.error("Cloudinary devolvió metadatos incompletos para un adjunto de conversación {}", conversacionId);
            if (publicId instanceof String storageKeyValido && !storageKeyValido.isBlank()) {
                eliminarAdjunto(storageKeyValido, resultado.get("resource_type"));
            }
            throw new ApiException(HttpStatus.BAD_GATEWAY, "ARCHIVO_NO_SUBIDO",
                "El proveedor de almacenamiento no devolvió una ubicación válida");
        }

        MensajeConversacion mensaje = new MensajeConversacion();
        mensaje.setConversacion(conversacion);
        mensaje.setRemitente(actor);
        mensaje.setCuerpo(cuerpo == null || cuerpo.isBlank() ? nombreArchivo : cuerpo.trim());
        mensaje.setArchivoUrl(url);
        mensaje.setArchivoClaveAlmacenamiento(storageKey);
        mensaje.setArchivoNombre(nombreArchivo);
        mensaje.setArchivoTipoMime(archivo.getContentType());
        mensaje.setArchivoTamanoBytes(archivo.getSize());
        try {
            mensaje = mensajeRepository.save(mensaje);
            conversacion.setActualizadoEn(LocalDateTime.now());
            return toMensajeDto(mensaje);
        } catch (RuntimeException exception) {
            eliminarAdjunto(storageKey, resultado.get("resource_type"));
            throw exception;
        }
    }

    private void eliminarAdjunto(String storageKey, Object resourceType) {
        try {
            cloudinary.uploader().destroy(storageKey, ObjectUtils.asMap(
                "resource_type", resourceType instanceof String ? resourceType : "raw"));
        } catch (Exception cleanupException) {
            log.warn("No se pudo eliminar el adjunto remoto {}", storageKey, cleanupException);
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty() || archivo.getSize() > MAX_FILE_BYTES) {
            throw ApiException.badRequest("TAMANO_ARCHIVO_INVALIDO",
                "El archivo debe tener contenido y no superar los 8 MB");
        }
        String nombre = nombreArchivo(archivo.getOriginalFilename());
        int extensionSeparator = nombre.lastIndexOf('.');
        String extension = extensionSeparator < 0 ? ""
            : nombre.substring(extensionSeparator + 1).toLowerCase(Locale.ROOT);
        String mimeType = archivo.getContentType();
        if (!ALLOWED_EXTENSIONS.contains(extension) || mimeType == null
                || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase(Locale.ROOT))) {
            throw ApiException.badRequest("TIPO_ARCHIVO_NO_PERMITIDO",
                "Adjunte una imagen, PDF, documento de Office o archivo de texto permitido");
        }
    }

    private String nombreArchivo(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "archivo";
        }
        String nombre = nombreOriginal.replace('\\', '/');
        nombre = nombre.substring(nombre.lastIndexOf('/') + 1).trim();
        if (nombre.isBlank()) {
            return "archivo";
        }
        return nombre.length() > 255 ? nombre.substring(nombre.length() - 255) : nombre;
    }

    private Conversacion verificarMiembro(UUID conversacionId, Usuario actor) {
        return miembroRepository.findByConversacion_IdAndUsuario_IdAndSalioEnIsNull(conversacionId, actor.getId())
            .map(MiembroConversacion::getConversacion)
            .orElseThrow(() -> ApiException.notFound("CONVERSACION_NO_ENCONTRADA", "Conversación no encontrada"));
    }

    private ConversacionDTO toDto(Conversacion conversacion) {
        List<ConversacionDTO.ParticipanteDTO> participantes =
            miembroRepository.findAllByConversacion_IdAndSalioEnIsNull(conversacion.getId()).stream()
                .map(MiembroConversacion::getUsuario)
                .map(usuario -> new ConversacionDTO.ParticipanteDTO(usuario.getId(),
                    usuario.getNombres() + " " + usuario.getApellidos(), usuario.getCorreo()))
                .toList();
        return new ConversacionDTO(conversacion.getId(), conversacion.getTipo(), conversacion.getNombre(),
            conversacion.getCreadoPor().getId(), participantes, conversacion.getCreadoEn(),
            conversacion.getActualizadoEn());
    }

    private MensajeConversacionDTO toMensajeDto(MensajeConversacion mensaje) {
        return new MensajeConversacionDTO(mensaje.getId(), mensaje.getConversacion().getId(),
            mensaje.getRemitente().getId(),
            mensaje.getRemitente().getNombres() + " " + mensaje.getRemitente().getApellidos(),
            mensaje.getCuerpo(), mensaje.getArchivoUrl(),
            mensaje.getArchivoNombre(), mensaje.getArchivoTipoMime(), mensaje.getArchivoTamanoBytes(),
            mensaje.getCreadoEn(), mensaje.getEditadoEn());
    }
}
