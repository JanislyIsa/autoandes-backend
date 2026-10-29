package pe.edu.upeu.autoandes.service.impl;

import java.math.BigDecimal;
import java.time.*;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;
import pe.edu.upeu.autoandes.exception.*;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.OrdenService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class OrdenServiceImpl implements OrdenService {
    private final OrdenServicioRepository ordenes;
    private final VehiculoRepository vehiculos;
    private final ServicioRepository servicios;
    private final AutoAndesMapper mapper;
    private final Clock reloj;

    @Override
    @Transactional
    public OrdenResponseDTO registrar(OrdenRequestDTO r, String username) {
        validarDetalles(r);
        // Serializa registros del mismo vehículo para validar el kilometraje contra el último confirmado.
        Vehiculo vehiculo = vehiculos.buscarParaRegistro(r.vehiculoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado: " + r.vehiculoId()));
        ordenes.findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(vehiculo.getId(), EstadoOrden.REGISTRADA)
                .ifPresent(ultima -> {
                    if (r.kilometraje() < ultima.getKilometraje()) {
                        throw new ReglaNegocioException("El kilometraje no puede ser menor al último registrado: "
                                + ultima.getKilometraje());
                    }
                });

        OrdenServicio orden = new OrdenServicio();
        orden.setVehiculo(vehiculo);
        orden.setFecha(LocalDateTime.now(reloj));
        orden.setKilometraje(r.kilometraje());
        orden.setEstado(EstadoOrden.REGISTRADA);
        orden.setUsuarioRegistro(username);
        BigDecimal total = BigDecimal.ZERO;

        for (DetalleOrdenRequestDTO item : r.detalles()) {
            Servicio servicio = servicios.findById(item.servicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado: " + item.servicioId()));
            if (!Boolean.TRUE.equals(servicio.getActivo())) {
                throw new ReglaNegocioException("El servicio " + servicio.getCodigo() + " está inactivo");
            }
            DetalleOrden detalle = new DetalleOrden();
            detalle.setServicio(servicio);
            detalle.setCantidad(item.cantidad());
            detalle.setPrecioUnitario(servicio.getPrecio());
            detalle.setSubtotal(servicio.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            orden.agregarDetalle(detalle);
            total = total.add(detalle.getSubtotal());
        }
        if (total.compareTo(new BigDecimal("99999999.99")) > 0) {
            throw new DatosInvalidosException("El total excede el importe máximo permitido");
        }
        Long correlativo = ordenes.siguienteNumero();
        if (correlativo > 999999) throw new ReglaNegocioException("Se agotó la numeración de órdenes");
        orden.setNumero("OS-%06d".formatted(correlativo));
        orden.setTotal(total);
        OrdenServicio guardada = ordenes.saveAndFlush(orden);
        log.info("Orden registrada: número={}, vehículo={}, total={}", guardada.getNumero(), vehiculo.getPlaca(), total);
        return mapper.orden(guardada);
    }

    private void validarDetalles(OrdenRequestDTO r) {
        if (r.detalles() == null || r.detalles().isEmpty() || r.detalles().size() > 10) {
            throw new DatosInvalidosException("Una orden debe tener entre 1 y 10 detalles");
        }
        var serviciosUsados = new HashSet<Long>();
        for (DetalleOrdenRequestDTO item : r.detalles()) {
            if (item == null || item.servicioId() == null || item.cantidad() == null
                    || item.cantidad() < 1 || item.cantidad() > 5) {
                throw new DatosInvalidosException("Cada detalle requiere un servicio y cantidad entre 1 y 5");
            }
            if (!serviciosUsados.add(item.servicioId())) {
                throw new DatosInvalidosException("No se puede repetir el mismo servicio en la orden");
            }
        }
    }

    @Override
    @Transactional
    public OrdenResponseDTO anular(Long id, AnularOrdenRequestDTO r) {
        if (r.motivo() == null || r.motivo().trim().length() < 10 || r.motivo().trim().length() > 200) {
            throw new DatosInvalidosException("El motivo debe tener entre 10 y 200 caracteres");
        }
        OrdenServicio orden = ordenes.buscarParaAnular(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada: " + id));
        if (orden.getEstado() != EstadoOrden.REGISTRADA) {
            throw new ReglaNegocioException("Solo se puede anular una orden REGISTRADA");
        }
        if (!orden.getFecha().toLocalDate().equals(LocalDate.now(reloj))) {
            throw new ReglaNegocioException("Solo se puede anular una orden el mismo día de su registro");
        }
        orden.setEstado(EstadoOrden.ANULADA);
        orden.setMotivoAnulacion(r.motivo().trim());
        log.info("Orden anulada: número={}", orden.getNumero());
        return mapper.orden(orden);
    }

    @Override
    public OrdenResponseDTO buscar(Long id) {
        return mapper.orden(ordenes.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada: " + id)));
    }

    @Override
    public List<OrdenResponseDTO> consultar(String placa, EstadoOrden estado, LocalDate desde, LocalDate hasta) {
        validarRango(desde, hasta);
        String filtroPlaca = placa == null || placa.isBlank() ? null : placa.trim();
        return ordenes.buscar(filtroPlaca, estado, desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay()).stream().map(mapper::orden).toList();
    }

    static void validarRango(LocalDate desde, LocalDate hasta) {
        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new DatosInvalidosException("La fecha desde no puede ser posterior a hasta");
        }
    }
}
