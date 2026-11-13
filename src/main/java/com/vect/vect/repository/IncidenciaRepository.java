package com.vect.vect.repository;

import com.vect.vect.entity.Incidencia;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface IncidenciaRepository extends JpaRepository<Incidencia, UUID> {

    List<Incidencia> findAllByOrderByCreadoEnDesc();

    List<Incidencia> findAllByReportante_IdOrderByCreadoEnDesc(UUID reportanteId);

    List<Incidencia> findAllByTecnicoAsignado_IdOrderByCreadoEnDesc(UUID tecnicoId);

    @EntityGraph(attributePaths = {"categoria", "ubicacion", "reportante"})
    @Query("""
        select i from Incidencia i
        left join fetch i.tecnicoAsignado
        where (
            :globalAccess = true
            or (:technician = true
                and i.tecnicoAsignado.id = :actorId)
            or (:technician = false
                and i.reportante.id = :actorId)
        )
        and (:query = ''
            or lower(i.codigo) like lower(concat('%', :query, '%'))
            or lower(i.titulo) like lower(concat('%', :query, '%'))
            or lower(concat(i.reportante.nombres, ' ', i.reportante.apellidos))
                like lower(concat('%', :query, '%'))
            or lower(concat(i.tecnicoAsignado.nombres, ' ', i.tecnicoAsignado.apellidos))
                like lower(concat('%', :query, '%')))
        and (cast(:estado as string) is null or i.estado = :estado)
        """)
    Page<Incidencia> buscarPaginaVisible(@Param("globalAccess") boolean globalAccess,
                                         @Param("technician") boolean technician,
                                         @Param("actorId") UUID actorId,
                                         @Param("query") String query,
                                         @Param("estado") Incidencia.EstadoIncidencia estado,
                                         Pageable pageable);

    @Query(value = "SELECT nextval('public.seq_incidencia_2026')", nativeQuery = true)
    long siguienteNumeroCodigo();

    @Query(value = """
        SELECT EXISTS (
            SELECT 1
            FROM vect.transiciones_estado_incidencia
            WHERE rol::text = :rol
              AND estado_origen::text = :origen
              AND estado_destino::text = :destino
        )
        """, nativeQuery = true)
    boolean existeTransicion(@Param("rol") String rol, @Param("origen") String origen,
                             @Param("destino") String destino);

    @Query("select i.estado, count(i) from Incidencia i where i.creadoEn >= :desde group by i.estado")
    List<Object[]> contarPorEstado(@Param("desde") LocalDateTime desde);

    @Query("select i.estado, count(i) from Incidencia i group by i.estado")
    List<Object[]> contarTotalPorEstado();

    @Query("select i.categoria.nombre, count(i) from Incidencia i where i.creadoEn >= :desde group by i.categoria.nombre order by count(i) desc")
    List<Object[]> contarPorCategoria(@Param("desde") LocalDateTime desde);

    @Query("""
        select i.categoria.nombre, count(i) from Incidencia i
        where i.creadoEn >= :desde
          and (:globalAccess = true
               or (:technician = true
                   and i.tecnicoAsignado.id = :actorId)
               or (:technician = false
                   and i.reportante.id = :actorId))
        group by i.categoria.nombre order by count(i) desc
        """)
    List<Object[]> contarCategoriasVisibles(@Param("desde") LocalDateTime desde,
                                            @Param("globalAccess") boolean globalAccess,
                                            @Param("technician") boolean technician,
                                            @Param("actorId") UUID actorId);

    @Query("select i.prioridad, count(i) from Incidencia i where i.creadoEn >= :desde group by i.prioridad order by count(i) desc")
    List<Object[]> contarPorPrioridad(@Param("desde") LocalDateTime desde);

    @Query("""
        select i.estado, count(i) from Incidencia i
        where i.creadoEn >= :desde
          and (:globalAccess = true
               or (:technician = true
                   and i.tecnicoAsignado.id = :actorId)
               or (:technician = false
                   and i.reportante.id = :actorId))
        group by i.estado
        """)
    List<Object[]> contarEstadosVisibles(@Param("desde") LocalDateTime desde,
                                         @Param("globalAccess") boolean globalAccess,
                                         @Param("technician") boolean technician,
                                         @Param("actorId") UUID actorId);

    @Query("""
        select i.prioridad, count(i) from Incidencia i
        where i.creadoEn >= :desde
          and (:globalAccess = true
               or (:technician = true
                   and i.tecnicoAsignado.id = :actorId)
               or (:technician = false
                   and i.reportante.id = :actorId))
        group by i.prioridad
        """)
    List<Object[]> contarPrioridadesVisibles(@Param("desde") LocalDateTime desde,
                                             @Param("globalAccess") boolean globalAccess,
                                             @Param("technician") boolean technician,
                                             @Param("actorId") UUID actorId);

    @Query(value = """
        SELECT to_char(d.dia, 'YYYY-MM-DD') AS dia,
               COALESCE(c.creadas, 0) AS creadas,
               COALESCE(r.resueltas, 0) AS resueltas
        FROM generate_series(
            date_trunc('day', cast(:desde as timestamp)),
            date_trunc('day', cast(:hasta as timestamp)),
            interval '1 day'
        ) AS d(dia)
        LEFT JOIN (
            SELECT date_trunc('day', creado_en) AS dia, count(*) AS creadas
            FROM vect.incidencias
            WHERE creado_en >= cast(:desde as timestamp)
            GROUP BY 1
        ) c ON c.dia = d.dia
        LEFT JOIN (
            SELECT date_trunc('day', resuelto_en) AS dia, count(*) AS resueltas
            FROM vect.incidencias
            WHERE resuelto_en >= cast(:desde as timestamp)
            GROUP BY 1
        ) r ON r.dia = d.dia
        ORDER BY d.dia
        """, nativeQuery = true)
    List<Object[]> contarCreadasYResueltasPorDia(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDate hasta);

    @Query(value = """
        SELECT to_char(d.dia, 'YYYY-MM-DD') AS dia,
               COALESCE(c.creadas, 0) AS creadas,
               COALESCE(r.resueltas, 0) AS resueltas
        FROM generate_series(
            date_trunc('day', cast(:desde as timestamp)),
            date_trunc('day', cast(:hasta as timestamp)),
            interval '1 day'
        ) AS d(dia)
        LEFT JOIN (
            SELECT date_trunc('day', creado_en) AS dia, count(*) AS creadas
            FROM vect.incidencias
            WHERE creado_en >= cast(:desde as timestamp)
              AND (:globalAccess = true
                   OR (:technician = true AND tecnico_asignado_id = :actorId)
                   OR (:technician = false AND reportante_id = :actorId))
            GROUP BY 1
        ) c ON c.dia = d.dia
        LEFT JOIN (
            SELECT date_trunc('day', resuelto_en) AS dia, count(*) AS resueltas
            FROM vect.incidencias
            WHERE resuelto_en >= cast(:desde as timestamp)
              AND (:globalAccess = true
                   OR (:technician = true AND tecnico_asignado_id = :actorId)
                   OR (:technician = false AND reportante_id = :actorId))
            GROUP BY 1
        ) r ON r.dia = d.dia
        ORDER BY d.dia
        """, nativeQuery = true)
    List<Object[]> contarActividadVisiblePorDia(@Param("desde") LocalDateTime desde,
                                                @Param("hasta") LocalDate hasta,
                                                @Param("globalAccess") boolean globalAccess,
                                                @Param("technician") boolean technician,
                                                @Param("actorId") UUID actorId);
}
