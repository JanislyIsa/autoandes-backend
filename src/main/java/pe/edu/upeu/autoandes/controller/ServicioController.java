package pe.edu.upeu.autoandes.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.ServicioService;

@RestController
@RequestMapping("/api/v1/servicios")
@RequiredArgsConstructor
public class ServicioController {
    private final ServicioService servicio;
    @GetMapping
    public List<ServicioResponseDTO> listar() { return servicio.listar(); }
    @GetMapping("/{id}")
    public ServicioResponseDTO buscar(@PathVariable Long id) { return servicio.buscar(id); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicioResponseDTO crear(@Valid @RequestBody ServicioRequestDTO r) { return servicio.crear(r); }
    @PutMapping("/{id}")
    public ServicioResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequestDTO r) {
        return servicio.actualizar(id, r);
    }
    @PatchMapping("/{id}/estado")
    public ServicioResponseDTO estado(@PathVariable Long id, @Valid @RequestBody EstadoServicioRequestDTO r) {
        return servicio.cambiarEstado(id, r);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { servicio.eliminar(id); }
}

