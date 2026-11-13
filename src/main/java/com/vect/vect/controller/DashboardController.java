package com.vect.vect.controller;

import com.vect.vect.dto.response.DashboardResumenDTO;
import com.vect.vect.dto.response.ReporteIncidenciasDTO;
import com.vect.vect.entity.Usuario;
import com.vect.vect.service.DashboardService;
import com.vect.vect.service.ReporteService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final ReporteService reporteService;

    public DashboardController(DashboardService dashboardService, ReporteService reporteService) {
        this.dashboardService = dashboardService;
        this.reporteService = reporteService;
    }

    @GetMapping("/resumen")
    public DashboardResumenDTO resumen() {
        return dashboardService.resumen();
    }

    @GetMapping("/actividad")
    public ReporteIncidenciasDTO actividad(
            Authentication authentication,
            @RequestParam(defaultValue = "7") int dias) {
        return reporteService.generar(dias, (Usuario) authentication.getPrincipal());
    }
}
