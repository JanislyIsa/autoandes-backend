package pe.edu.upeu.autoandes.controller;

import java.util.List;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.service.VehiculoService;

@RestController
@RequestMapping("/api/v1/vehiculos")
@RequiredArgsConstructor
public class VehiculoController {
    private final VehiculoService servicio;
    @GetMapping
    public List<VehiculoResponseDTO> listar(@RequestParam(required = false) Long clienteId) { return servicio.listar(clienteId); }
    @GetMapping("/{id}")
    public VehiculoResponseDTO buscar(@PathVariable Long id) { return servicio.buscar(id); }
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VehiculoResponseDTO crear(@Valid @RequestBody VehiculoRequestDTO r) { return servicio.crear(r); }
    @PutMapping("/{id}")
    public VehiculoResponseDTO actualizar(@PathVariable Long id, @Valid @RequestBody VehiculoRequestDTO r) {
        return servicio.actualizar(id, r);
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) { servicio.eliminar(id); }
}

