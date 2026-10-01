package com.vect.vect;

import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import com.vect.vect.entity.RegistroAuditoria;
import com.vect.vect.repository.IncidenciaRepository;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.service.DatabaseActorContext;
import com.vect.vect.service.AuditoriaService;
import com.vect.vect.service.HistorialSolicitudCambioService;
import com.vect.vect.service.ParametroSistemaService;
import com.vect.vect.service.ReglaAutomatizacionService;
import com.vect.vect.service.ReporteService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
class VectApplicationTests {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private IncidenciaRepository incidenciaRepository;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private DatabaseActorContext databaseActorContext;

	@Autowired
	private ReporteService reporteService;

	@Autowired
	private AuditoriaService auditoriaService;

	@Autowired
	private ParametroSistemaService parametroSistemaService;

	@Autowired
	private ReglaAutomatizacionService reglaAutomatizacionService;

	@Autowired
	private HistorialSolicitudCambioService historialSolicitudCambioService;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void postgresEnumQueriesAndTriggerContextWork() {
		Long result = entityManager.createQuery(
				"select count(i) from Incidencia i where i.estado = :estado", Long.class)
			.setParameter("estado", Incidencia.EstadoIncidencia.REGISTRADA)
			.getSingleResult();
		assertThat(result).isNotNull();
		assertThat(incidenciaRepository.existeTransicion("SUPERVISOR", "REGISTRADA", "ASIGNADA")).isTrue();

		Usuario actor = new Usuario();
		actor.setId(UUID.randomUUID());
		databaseActorContext.setTransition(actor, "test de transición");
		Object[] triggerContext = (Object[]) entityManager.createNativeQuery(
			"select current_setting('app.user_id', true), current_setting('app.motivo_transicion', true)")
			.getSingleResult();

		assertThat(triggerContext[0]).isEqualTo(actor.getId().toString());
		assertThat(triggerContext[1]).isEqualTo("test de transición");
	}

	@Test
	void reportQueriesRunAgainstTheNeonSchema() {
		var report = reporteService.generar(7);

		assertThat(report.total()).isGreaterThanOrEqualTo(0);
		assertThat(report.serieDiaria()).hasSize(7);
		assertThat(report.porEstado()).containsKeys("REGISTRADA", "ASIGNADA", "CERRADA");
	}

	@Test
	void newAdministrationQueriesRunAgainstExistingNeonTables() {
		assertThat(auditoriaService.listar(null, null, null, null, null, 0, 10)).isNotNull();
		assertThat(auditoriaService.listarParaExcel(null, null, null, null, null)).isNotNull();
		assertThat(parametroSistemaService.listar()).isNotNull();
		assertThat(reglaAutomatizacionService.listar()).isNotNull();
		assertThat(usuarioRepository.findAllByRolAndActivoTrueOrderByApellidosAscNombresAsc(
			Usuario.RolUsuario.TECNICO)).isNotNull();

		Usuario supervisor = new Usuario();
		supervisor.setRol(Usuario.RolUsuario.SUPERVISOR);
		assertThat(historialSolicitudCambioService.listar(null, supervisor)).isNotNull();
	}

	@Test
	@Transactional
	void auditWriteUsesExistingNeonTable() {
		Usuario actor = new Usuario();
		actor.setNombres("Prueba");
		actor.setApellidos("Auditoria");
		actor.setCorreo("audit-" + UUID.randomUUID() + "@example.test");
		actor.setHashContrasena("hash-no-usado");
		actor.setRol(Usuario.RolUsuario.ADMIN);
		actor = usuarioRepository.save(actor);

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getRequestURI()).thenReturn("/api/usuarios/" + UUID.randomUUID());
		when(request.getMethod()).thenReturn("PUT");
		when(request.getRemoteAddr()).thenReturn("127.0.0.1");
		when(request.getHeader("User-Agent")).thenReturn("schema-test");
		auditoriaService.registrarCambio(request, actor);
		entityManager.flush();

		Long auditCount = entityManager.createQuery(
				"select count(a) from RegistroAuditoria a where a.usuarioId = :usuarioId", Long.class)
			.setParameter("usuarioId", actor.getId())
			.getSingleResult();
		assertThat(auditCount).isEqualTo(1);
	}
}
