package pe.edu.upeu.autoandes.service.impl;

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
import pe.edu.upeu.autoandes.service.VehiculoService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class VehiculoServiceImpl implements VehiculoService {
    private final VehiculoRepository vehiculos;
    private final ClienteRepository clientes;
    private final OrdenServicioRepository ordenes;
    private final AutoAndesMapper mapper;

    @Override
    public List<VehiculoResponseDTO> listar(Long clienteId) {
        if (clienteId != null) obtenerCliente(clienteId);
        var lista = clienteId == null ? vehiculos.findAllByOrderByPlacaAsc()
                : vehiculos.findByClienteIdOrderByPlacaAsc(clienteId);
        return lista.stream().map(mapper::vehiculo).toList();
    }

    @Override
    public VehiculoResponseDTO buscar(Long id) {
        return mapper.vehiculo(obtener(id));
    }

    @Override
    @Transactional
    public VehiculoResponseDTO crear(VehiculoRequestDTO r) {
        if (vehiculos.existsByPlaca(r.placa())) {
            throw new ReglaNegocioException("Ya existe un vehículo con la placa " + r.placa());
        }
        Vehiculo vehiculo = new Vehiculo();
        mapper.actualizar(vehiculo, r, obtenerCliente(r.clienteId()));
        vehiculo = vehiculos.saveAndFlush(vehiculo);
        log.info("Vehículo creado: id={}, placa={}", vehiculo.getId(), vehiculo.getPlaca());
        return mapper.vehiculo(vehiculo);
    }

    @Override
    @Transactional
    public VehiculoResponseDTO actualizar(Long id, VehiculoRequestDTO r) {
        Vehiculo vehiculo = obtener(id);
        if (vehiculos.existsByPlacaAndIdNot(r.placa(), id)) {
            throw new ReglaNegocioException("Ya existe un vehículo con la placa " + r.placa());
        }
        mapper.actualizar(vehiculo, r, obtenerCliente(r.clienteId()));
        vehiculos.flush();
        log.info("Vehículo actualizado: id={}", id);
        return mapper.vehiculo(vehiculo);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Vehiculo vehiculo = obtener(id);
        if (ordenes.existsByVehiculoId(id)) {
            throw new ReglaNegocioException("El vehículo tiene órdenes registradas y no puede eliminarse");
        }
        vehiculos.delete(vehiculo);
        vehiculos.flush();
        log.info("Vehículo eliminado: id={}", id);
    }

    private Vehiculo obtener(Long id) {
        return vehiculos.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Vehículo no encontrado: " + id));
    }

    private Cliente obtenerCliente(Long id) {
        return clientes.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
    }
}

