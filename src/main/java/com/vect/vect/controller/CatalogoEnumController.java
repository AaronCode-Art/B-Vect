package com.vect.vect.controller;

import com.vect.vect.entity.Incidencia;
import com.vect.vect.entity.Usuario;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
public class CatalogoEnumController {

    @GetMapping("/api/estados-incidencia")
    public List<String> estadosIncidencia() {
        return Arrays.stream(Incidencia.EstadoIncidencia.values()).map(Enum::name).toList();
    }

    @GetMapping("/api/roles")
    public List<String> roles() {
        return Arrays.stream(Usuario.RolUsuario.values()).map(Enum::name).toList();
    }
}
