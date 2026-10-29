package pe.edu.upeu.autoandes.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.service.ReporteService;

@RestController
@RequestMapping("/api/v1/reportes")
@RequiredArgsConstructor
public class ReporteController {
    private final ReporteService servicio;
    @GetMapping("/ingresos-por-servicio")
    @PreAuthorize("hasAuthority('REPORTE_VER')")
    public List<IngresoPorServicioDTO> ingresos(@RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return servicio.ingresos(desde, hasta);
    }
}
