package pe.edu.upeu.autoandes.service;

import java.time.LocalDate;
import java.util.List;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.EstadoOrden;

public interface OrdenService {
    OrdenResponseDTO registrar(OrdenRequestDTO request, String username);
    OrdenResponseDTO anular(Long id, AnularOrdenRequestDTO request);
    OrdenResponseDTO buscar(Long id);
    List<OrdenResponseDTO> consultar(String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta);
}
