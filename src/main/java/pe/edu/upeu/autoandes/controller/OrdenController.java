package pe.edu.upeu.autoandes.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.enums.EstadoOrden;
import pe.edu.upeu.autoandes.service.OrdenService;

@RestController
@RequestMapping("/api/v1/ordenes")
@RequiredArgsConstructor
public class OrdenController {
    private final OrdenService servicio;
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrdenResponseDTO registrar(@Valid @RequestBody OrdenRequestDTO r, Principal usuario) {
        return servicio.registrar(r, usuario.getName());
    }
    @GetMapping
    public List<OrdenResponseDTO> consultar(@RequestParam(required = false) String placa,
            @RequestParam(required = false) EstadoOrden estado, @RequestParam(required = false) LocalDate desde,
            @RequestParam(required = false) LocalDate hasta) {
        return servicio.consultar(placa, estado, desde, hasta);
    }
    @GetMapping("/{id}")
    public OrdenResponseDTO buscar(@PathVariable Long id) { return servicio.buscar(id); }
    @PatchMapping("/{id}/anular")
    public OrdenResponseDTO anular(@PathVariable Long id, @Valid @RequestBody AnularOrdenRequestDTO r) {
        return servicio.anular(id, r);
    }
}
