package com.vect.vect.service;

import com.vect.vect.common.exception.ApiException;
import com.vect.vect.dto.response.RegistroAuditoriaDTO;
import com.vect.vect.entity.RegistroAuditoria;
import com.vect.vect.entity.Usuario;
import com.vect.vect.mapper.RegistroAuditoriaMapper;
import com.vect.vect.repository.RegistroAuditoriaRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class AuditoriaService {

    public static final int MAX_PAGE_SIZE = 200;
    public static final int MAX_EXPORT_ROWS = 10000;

    private final RegistroAuditoriaRepository repository;
    private final RegistroAuditoriaMapper mapper;

    public AuditoriaService(RegistroAuditoriaRepository repository, RegistroAuditoriaMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Page<RegistroAuditoriaDTO> listar(UUID usuarioId, String tipoEntidad, String accion,
                                              LocalDate desde, LocalDate hasta, int page, int size) {
        validarFiltro(desde, hasta);
        if (page < 0 || size < 1 || size > MAX_PAGE_SIZE) {
            throw ApiException.badRequest("PAGINACION_INVALIDA",
                "La página debe ser no negativa y el tamaño debe estar entre 1 y " + MAX_PAGE_SIZE);
        }
        Page<RegistroAuditoria> registros = repository.findAll(
            filtro(usuarioId, tipoEntidad, accion, desde, hasta),
            PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creadoEn")));
        return registros.map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<RegistroAuditoriaDTO> listarParaExcel(UUID usuarioId, String tipoEntidad, String accion,
                                                       LocalDate desde, LocalDate hasta) {
        validarFiltro(desde, hasta);
        Specification<RegistroAuditoria> filtro = filtro(usuarioId, tipoEntidad, accion, desde, hasta);
        if (repository.count(filtro) > MAX_EXPORT_ROWS) {
            throw ApiException.badRequest("EXPORTACION_DEMASIADO_GRANDE",
                "La exportación excede " + MAX_EXPORT_ROWS + " registros; aplique filtros de fecha o usuario");
        }
        return repository.findAll(filtro, PageRequest.of(0, MAX_EXPORT_ROWS,
                Sort.by(Sort.Direction.DESC, "creadoEn")))
            .getContent().stream().map(mapper::toDto).toList();
    }

    @Transactional
    public void registrarCambio(HttpServletRequest request, Usuario actor) {
        String[] segmentos = request.getRequestURI().split("/");
        String tipoEntidad = segmentos.length > 2 ? segmentos[2] : "api";
        String ruta = Arrays.stream(segmentos).filter(segmento -> !segmento.isEmpty())
            .map(segmento -> esUuid(segmento) ? "{id}" : segmento)
            .collect(java.util.stream.Collectors.joining("/", "/", ""));
        String accion = request.getMethod() + " " + ruta;
        if (accion.length() > 100) {
            accion = accion.substring(0, 100);
        }

        RegistroAuditoria registro = new RegistroAuditoria();
        registro.setUsuarioId(actor.getId());
        registro.setAccion(accion);
        registro.setTipoEntidad(tipoEntidad);
        registro.setEntidadId(encontrarId(segmentos));
        registro.setDireccionIp(direccionIp(request.getRemoteAddr()));
        registro.setAgenteUsuario(request.getHeader("User-Agent"));
        repository.save(registro);
    }

    private Specification<RegistroAuditoria> filtro(UUID usuarioId, String tipoEntidad, String accion,
                                                       LocalDate desde, LocalDate hasta) {
        return (root, query, builder) -> {
            var predicates = builder.conjunction();
            if (usuarioId != null) {
                predicates = builder.and(predicates, builder.equal(root.get("usuarioId"), usuarioId));
            }
            if (tipoEntidad != null && !tipoEntidad.isBlank()) {
                predicates = builder.and(predicates,
                    builder.equal(root.get("tipoEntidad"), tipoEntidad.trim()));
            }
            if (accion != null && !accion.isBlank()) {
                predicates = builder.and(predicates, builder.equal(root.get("accion"), accion.trim()));
            }
            if (desde != null) {
                predicates = builder.and(predicates,
                    builder.greaterThanOrEqualTo(root.get("creadoEn"), desde.atStartOfDay()));
            }
            if (hasta != null) {
                predicates = builder.and(predicates,
                    builder.lessThan(root.get("creadoEn"), hasta.plusDays(1).atStartOfDay()));
            }
            return predicates;
        };
    }

    private void validarFiltro(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw ApiException.badRequest("RANGO_FECHAS_INVALIDO", "La fecha desde no puede ser posterior a hasta");
        }
    }

    private UUID encontrarId(String[] segmentos) {
        for (String segmento : segmentos) {
            try {
                return UUID.fromString(segmento);
            } catch (IllegalArgumentException ignored) {
                // Route segments that are not UUIDs are expected.
            }
        }
        return null;
    }

    private boolean esUuid(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private InetAddress direccionIp(String remoteAddress) {
        try {
            return InetAddress.getByName(remoteAddress);
        } catch (UnknownHostException exception) {
            throw new IllegalStateException("La dirección remota no es una IP válida", exception);
        }
    }
}
