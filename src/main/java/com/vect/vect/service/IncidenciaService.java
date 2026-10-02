package com.vect.vect.service;

import com.vect.vect.dto.request.IncidenciaCreateRequest;
import com.vect.vect.dto.response.IncidenciaDTO;
import com.vect.vect.common.exception.ApiException;
import com.vect.vect.entity.*;
import com.vect.vect.mapper.IncidenciaMapper;
import com.vect.vect.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final CategoriaRepository categoriaRepository;
    private final UbicacionRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;
    private final HistorialIncidenciaRepository historialIncidenciaRepository;
    private final IncidenciaMapper incidenciaMapper;
    private final DatabaseActorContext databaseActorContext;

    public IncidenciaService(IncidenciaRepository incidenciaRepository,
                             CategoriaRepository categoriaRepository,
                             UbicacionRepository ubicacionRepository,
                             UsuarioRepository usuarioRepository,
                             HistorialIncidenciaRepository historialIncidenciaRepository,
                             IncidenciaMapper incidenciaMapper,
                             DatabaseActorContext databaseActorContext) {
        this.incidenciaRepository = incidenciaRepository;
        this.categoriaRepository = categoriaRepository;
        this.ubicacionRepository = ubicacionRepository;
        this.usuarioRepository = usuarioRepository;
        this.historialIncidenciaRepository = historialIncidenciaRepository;
        this.incidenciaMapper = incidenciaMapper;
        this.databaseActorContext = databaseActorContext;
    }

    @Transactional(readOnly = true)
    public List<IncidenciaDTO> listar(Usuario actor) {
        List<Incidencia> incidencias = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> incidenciaRepository.findAllByOrderByCreadoEnDesc();
            case TECNICO -> incidenciaRepository.findAllByTecnicoAsignado_IdOrderByCreadoEnDesc(actor.getId());
            case EMPLEADO -> incidenciaRepository.findAllByReportante_IdOrderByCreadoEnDesc(actor.getId());
        };
        return incidencias.stream().map(incidenciaMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public IncidenciaDTO obtener(UUID id, Usuario actor) {
        Incidencia incidencia = buscar(id);
        verificarVisibilidad(incidencia, actor);
        return incidenciaMapper.toDto(incidencia);
    }

    @Transactional
    public IncidenciaDTO crear(IncidenciaCreateRequest request, Usuario reportante) {
        Categoria categoria = categoriaRepository.findById(request.categoriaId())
            .orElseThrow(() -> ApiException.notFound("CATEGORIA_NO_ENCONTRADA", "Categoría no encontrada"));

        Ubicacion ubicacion = request.ubicacionId() != null
            ? ubicacionRepository.findById(request.ubicacionId())
                .orElseThrow(() -> ApiException.notFound("UBICACION_NO_ENCONTRADA", "Ubicación no encontrada"))
            : null;

        Incidencia incidencia = new Incidencia();
        incidencia.setCodigo(generarCodigo());
        incidencia.setTitulo(request.titulo());
        incidencia.setDescripcion(request.descripcion());
        incidencia.setCategoria(categoria);
        incidencia.setUbicacion(ubicacion);
        incidencia.setCanal(request.canal());
        incidencia.setImpacto(request.impacto());
        incidencia.setUrgencia(request.urgencia());
        incidencia.setPrioridad(calcularPrioridad(incidencia.getImpacto(), incidencia.getUrgencia()));
        incidencia.setEstado(Incidencia.EstadoIncidencia.REGISTRADA);
        incidencia.setReportante(reportante);
        incidencia.setCreadoPor(reportante.getId());

        incidencia = incidenciaRepository.save(incidencia);
        return incidenciaMapper.toDto(incidencia);
    }

    @Transactional
    public IncidenciaDTO cambiarEstado(UUID id, Incidencia.EstadoIncidencia estadoNuevo, String motivo, Usuario actor) {
        Incidencia incidencia = buscar(id);
        verificarVisibilidad(incidencia, actor);

        if (motivo == null || motivo.isBlank()) {
            throw ApiException.badRequest("MOTIVO_REQUERIDO", "El motivo del cambio de estado es obligatorio");
        }

        if (actor.getRol() == Usuario.RolUsuario.EMPLEADO
                && estadoNuevo == Incidencia.EstadoIncidencia.CERRADA) {
            throw ApiException.conflict("TRANSICION_NO_PERMITIDA",
                "Los empleados no pueden cerrar incidencias");
        }

        if (!incidenciaRepository.existeTransicion(actor.getRol().name(),
                incidencia.getEstado().name(), estadoNuevo.name())) {
            throw ApiException.conflict("TRANSICION_NO_PERMITIDA",
                "No se permite cambiar de " + incidencia.getEstado() + " a " + estadoNuevo
                    + " para el rol " + actor.getRol());
        }

        databaseActorContext.setTransition(actor, motivo.trim());
        incidencia.setEstado(estadoNuevo);
        if (estadoNuevo == Incidencia.EstadoIncidencia.RESUELTA) {
            incidencia.setResueltoEn(LocalDateTime.now());
        } else if (estadoNuevo == Incidencia.EstadoIncidencia.CERRADA) {
            incidencia.setCerradoEn(LocalDateTime.now());
        } else if (estadoNuevo == Incidencia.EstadoIncidencia.ASIGNADA) {
            incidencia.setResueltoEn(null);
            incidencia.setCerradoEn(null);
        }

        incidencia = incidenciaRepository.save(incidencia);
        return incidenciaMapper.toDto(incidencia);
    }

    @Transactional
    public IncidenciaDTO asignarTecnico(UUID id, UUID tecnicoId, Usuario actor) {
        Incidencia incidencia = buscar(id);

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
            .orElseThrow(() -> ApiException.notFound("TECNICO_NO_ENCONTRADO", "Técnico no encontrado"));

        if (tecnico.getRol() != Usuario.RolUsuario.TECNICO || !Boolean.TRUE.equals(tecnico.getActivo())) {
            throw ApiException.badRequest("TECNICO_INVALIDO", "El usuario seleccionado no es un técnico activo");
        }
        if (incidencia.getEstado() != Incidencia.EstadoIncidencia.REGISTRADA
                && incidencia.getEstado() != Incidencia.EstadoIncidencia.ASIGNADA) {
            throw ApiException.conflict("INCIDENCIA_NO_ASIGNABLE",
                "Solo se puede asignar o reasignar una incidencia registrada o asignada");
        }

        String motivo = "Técnico asignado: " + tecnico.getNombres() + " " + tecnico.getApellidos();
        if (incidencia.getEstado() == Incidencia.EstadoIncidencia.REGISTRADA
                && !incidenciaRepository.existeTransicion(actor.getRol().name(),
                    Incidencia.EstadoIncidencia.REGISTRADA.name(), Incidencia.EstadoIncidencia.ASIGNADA.name())) {
            throw ApiException.conflict("TRANSICION_NO_PERMITIDA",
                "El rol no tiene permiso para asignar esta incidencia");
        }

        databaseActorContext.setTransition(actor, motivo);
        Incidencia.EstadoIncidencia estadoAnterior = incidencia.getEstado();
        incidencia.setTecnicoAsignado(tecnico);
        incidencia.setAsignadoEn(LocalDateTime.now());
        incidencia.setEstado(Incidencia.EstadoIncidencia.ASIGNADA);

        incidencia = incidenciaRepository.save(incidencia);

        if (estadoAnterior == Incidencia.EstadoIncidencia.ASIGNADA) {
            HistorialIncidencia historial = new HistorialIncidencia();
            historial.setIncidencia(incidencia);
            historial.setTipoEvento("TECNICO_REASIGNADO");
            historial.setEstadoOrigen(estadoAnterior);
            historial.setEstadoDestino(estadoAnterior);
            historial.setMotivo(motivo);
            historial.setRealizadoPor(actor);
            historialIncidenciaRepository.save(historial);
        }

        return incidenciaMapper.toDto(incidencia);
    }

    private String generarCodigo() {
        return formatearCodigo(incidenciaRepository.siguienteNumeroCodigo());
    }

    static String formatearCodigo(long numero) {
        return "INC-" + String.format(java.util.Locale.ROOT, "%04d", numero);
    }

    static Incidencia.NivelPrioridad calcularPrioridad(
            Incidencia.NivelPrioridad impacto, Incidencia.NivelPrioridad urgencia) {
        int nivel = Math.min(Incidencia.NivelPrioridad.values().length - 1,
            (impacto.ordinal() + urgencia.ordinal() + 1) / 2);
        return Incidencia.NivelPrioridad.values()[nivel];
    }

    private Incidencia buscar(UUID id) {
        return incidenciaRepository.findById(id)
            .orElseThrow(() -> ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada"));
    }

    private void verificarVisibilidad(Incidencia incidencia, Usuario actor) {
        boolean visible = switch (actor.getRol()) {
            case ADMIN, GERENCIA, SUPERVISOR -> true;
            case TECNICO -> incidencia.getTecnicoAsignado() != null
                && incidencia.getTecnicoAsignado().getId().equals(actor.getId());
            case EMPLEADO -> incidencia.getReportante().getId().equals(actor.getId());
        };
        if (!visible) {
            throw ApiException.notFound("INCIDENCIA_NO_ENCONTRADA", "Incidencia no encontrada");
        }
    }
}
