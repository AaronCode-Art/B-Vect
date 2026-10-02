package com.vect.vect.controller;

import com.vect.vect.dto.response.RegistroAuditoriaDTO;
import com.vect.vect.service.AuditoriaService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
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
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditoria")
@PreAuthorize("hasRole('ADMIN')")
public class AuditoriaController {

    private static final String EXCEL_MEDIA_TYPE =
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final AuditoriaService service;

    public AuditoriaController(AuditoriaService service) {
        this.service = service;
    }

    @GetMapping
    public Page<RegistroAuditoriaDTO> listar(
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) String tipoEntidad,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        return service.listar(usuarioId, tipoEntidad, accion, desde, hasta, page, size);
    }

    @GetMapping(value = "/excel", produces = EXCEL_MEDIA_TYPE)
    public ResponseEntity<byte[]> exportar(
            @RequestParam(required = false) UUID usuarioId,
            @RequestParam(required = false) String tipoEntidad,
            @RequestParam(required = false) String accion,
            @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) throws IOException {
        var registros = service.listarParaExcel(usuarioId, tipoEntidad, accion, desde, hasta);
        try (XSSFWorkbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Auditoría");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "Usuario ID", "Acción", "Tipo entidad", "Entidad ID",
                "Valores anteriores", "Valores nuevos", "Dirección IP", "Agente usuario", "Fecha"};
            for (int index = 0; index < columns.length; index++) {
                header.createCell(index).setCellValue(columns[index]);
            }
            for (int index = 0; index < registros.size(); index++) {
                RegistroAuditoriaDTO registro = registros.get(index);
                Row row = sheet.createRow(index + 1);
                set(row, 0, registro.id());
                set(row, 1, registro.usuarioId());
                set(row, 2, registro.accion());
                set(row, 3, registro.tipoEntidad());
                set(row, 4, registro.entidadId());
                set(row, 5, registro.valoresAnteriores());
                set(row, 6, registro.valoresNuevos());
                set(row, 7, registro.direccionIp());
                set(row, 8, registro.agenteUsuario());
                set(row, 9, registro.creadoEn());
            }
            workbook.write(output);
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"auditoria.xlsx\"")
                .contentType(MediaType.parseMediaType(EXCEL_MEDIA_TYPE))
                .body(output.toByteArray());
        }
    }

    private void set(Row row, int cellIndex, Object value) {
        if (value != null) {
            row.createCell(cellIndex).setCellValue(value.toString());
        }
    }
}
