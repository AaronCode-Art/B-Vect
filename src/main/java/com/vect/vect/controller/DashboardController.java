package com.vect.vect.controller;

import com.vect.vect.dto.response.DashboardResumenDTO;
import com.vect.vect.service.DashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumen")
    public DashboardResumenDTO resumen() {
        return dashboardService.resumen();
    }
}
