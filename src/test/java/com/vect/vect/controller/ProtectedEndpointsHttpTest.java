package com.vect.vect.controller;

import com.vect.vect.entity.Usuario;
import com.vect.vect.repository.UsuarioRepository;
import com.vect.vect.security.JwtService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "jwt.secret=integration-test-secret-with-at-least-32-bytes")
@AutoConfigureMockMvc
class ProtectedEndpointsHttpTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private JwtService jwtService;

    private final List<UUID> testUserIds = new ArrayList<>();

    @AfterEach
    void cleanUpUsers() {
        usuarioRepository.deleteAllById(testUserIds);
    }

    @Test
    void adminRoutesRequireAuthenticationAndRejectEmployeeRole() throws Exception {
        Usuario empleado = crearUsuario(Usuario.RolUsuario.EMPLEADO);
        String token = jwtService.generateAccessToken(empleado.getId(), empleado.getCorreo());

        mockMvc.perform(get("/api/parametros"))
            .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/parametros").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/reglas-automatizacion").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/auditoria").header("Authorization", "Bearer " + token))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanReadSchemaBackedAdministrationRoutes() throws Exception {
        Usuario admin = crearUsuario(Usuario.RolUsuario.ADMIN);
        String token = jwtService.generateAccessToken(admin.getId(), admin.getCorreo());

        mockMvc.perform(get("/api/parametros").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/reglas-automatizacion").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/auditoria").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/evidencias").header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }

    @Test
    void swaggerUiAndOpenApiDefinitionArePublic() throws Exception {
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/api-docs"))
            .andExpect(status().isOk());
    }

    @Test
    void approvalHistoryAllowsSupervisorButNotEmployee() throws Exception {
        Usuario empleado = crearUsuario(Usuario.RolUsuario.EMPLEADO);
        Usuario supervisor = crearUsuario(Usuario.RolUsuario.SUPERVISOR);
        String employeeToken = jwtService.generateAccessToken(empleado.getId(), empleado.getCorreo());
        String supervisorToken = jwtService.generateAccessToken(supervisor.getId(), supervisor.getCorreo());

        mockMvc.perform(get("/api/historial/aprobaciones")
                .header("Authorization", "Bearer " + employeeToken))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/historial/aprobaciones")
                .header("Authorization", "Bearer " + supervisorToken))
            .andExpect(status().isOk());
    }

    private Usuario crearUsuario(Usuario.RolUsuario rol) {
        Usuario usuario = new Usuario();
        usuario.setNombres("Prueba");
        usuario.setApellidos("HTTP " + rol.name());
        usuario.setCorreo("http-" + UUID.randomUUID() + "@example.test");
        usuario.setHashContrasena("not-used-by-this-test");
        usuario.setRol(rol);
        usuario.setActivo(true);
        usuario.setDebeCambiarContrasena(false);
        Usuario guardado = usuarioRepository.saveAndFlush(usuario);
        testUserIds.add(guardado.getId());
        return guardado;
    }
}
