package com.vect.vect.controller;

import com.vect.vect.dto.response.ReporteIncidenciasDTO;
import com.vect.vect.service.ReporteService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENCIA', 'SUPERVISOR')")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/operativo")
    public ReporteIncidenciasDTO operativo(@RequestParam(defaultValue = "30") int dias) {
        return reporteService.generar(dias);
    }

    @GetMapping("/completo")
    public ReporteIncidenciasDTO completo(@RequestParam(defaultValue = "90") int dias) {
        return reporteService.generar(dias);
    }

    @GetMapping(value = "/excel/operativo", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excelOperativo(@RequestParam(defaultValue = "30") int dias) throws IOException {
        return exportar(reporteService.generar(dias), "reporte-operativo.xlsx");
    }

    @GetMapping(value = "/excel/recurrentes", produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excelRecurrentes(@RequestParam(defaultValue = "90") int dias) throws IOException {
        return exportar(reporteService.generar(dias), "incidencias-recurrentes.xlsx");
    }

    private ResponseEntity<byte[]> exportar(ReporteIncidenciasDTO reporte, String filename) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet resumen = workbook.createSheet("Resumen");
            resumen.createRow(0).createCell(0).setCellValue("Total de incidencias");
            resumen.getRow(0).createCell(1).setCellValue(reporte.total());
            resumen.createRow(1).createCell(0).setCellValue("Desde");
            resumen.getRow(1).createCell(1).setCellValue(reporte.desde().toString());
            resumen.createRow(2).createCell(0).setCellValue("Hasta");
            resumen.getRow(2).createCell(1).setCellValue(reporte.hasta().toString());

            escribirDistribucion(workbook.createSheet("Por estado"), reporte.porEstado());
            escribirDistribucion(workbook.createSheet("Por categoría"), reporte.porCategoria());
            escribirDistribucion(workbook.createSheet("Por prioridad"), reporte.porPrioridad());

            Sheet serie = workbook.createSheet("Serie diaria");
            Row header = serie.createRow(0);
            header.createCell(0).setCellValue("Fecha");
            header.createCell(1).setCellValue("Creadas");
            header.createCell(2).setCellValue("Resueltas");
            for (int index = 0; index < reporte.serieDiaria().size(); index++) {
                var dato = reporte.serieDiaria().get(index);
                Row row = serie.createRow(index + 1);
                row.createCell(0).setCellValue(dato.fecha().toString());
                row.createCell(1).setCellValue(dato.creadas());
                row.createCell(2).setCellValue(dato.resueltas());
            }
            workbook.write(output);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(output.toByteArray());
        }
    }

    private void escribirDistribucion(Sheet sheet, Map<String, Long> distribucion) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Grupo");
        header.createCell(1).setCellValue("Cantidad");
        int index = 1;
        for (var entrada : distribucion.entrySet()) {
            Row row = sheet.createRow(index++);
            row.createCell(0).setCellValue(entrada.getKey());
            row.createCell(1).setCellValue(entrada.getValue());
        }
    }
}
