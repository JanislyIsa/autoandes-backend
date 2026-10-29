package pe.edu.upeu.autoandes.dto;

import java.math.BigDecimal;
import java.util.List;
import java.time.LocalDateTime;

public record OrdenResponseDTO(Long id, String numero, LocalDateTime fecha, Long vehiculoId, String placa, String clienteNombre,
        Integer kilometraje, String estado, BigDecimal total, String usuarioRegistro, String motivoAnulacion,
        List<DetalleOrdenResponseDTO> detalles) {}

