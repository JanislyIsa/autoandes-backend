package pe.edu.upeu.autoandes.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.Servicio;
import pe.edu.upeu.autoandes.exception.*;
import pe.edu.upeu.autoandes.mapper.AutoAndesMapper;
import pe.edu.upeu.autoandes.repository.*;
import pe.edu.upeu.autoandes.service.ServicioService;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ServicioServiceImpl implements ServicioService {
    private final ServicioRepository servicios;
    private final DetalleOrdenRepository detalles;
    private final AutoAndesMapper mapper;

    @Override
    public List<ServicioResponseDTO> listar() {
        return servicios.findAll(Sort.by("codigo")).stream().map(mapper::servicio).toList();
    }

    @Override
    public ServicioResponseDTO buscar(Long id) {
        return mapper.servicio(obtener(id));
    }

    @Override
    @Transactional
    public ServicioResponseDTO crear(ServicioRequestDTO r) {
        if (servicios.existsByCodigo(r.codigo())) {
            throw new ReglaNegocioException("Ya existe un servicio con el código " + r.codigo());
        }
        Servicio servicio = new Servicio();
        mapper.actualizar(servicio, r);
        servicio = servicios.saveAndFlush(servicio);
        log.info("Servicio creado: id={}, código={}", servicio.getId(), servicio.getCodigo());
        return mapper.servicio(servicio);
    }

    @Override
    @Transactional
    public ServicioResponseDTO actualizar(Long id, ServicioRequestDTO r) {
        Servicio servicio = obtener(id);
        if (servicios.existsByCodigoAndIdNot(r.codigo(), id)) {
            throw new ReglaNegocioException("Ya existe un servicio con el código " + r.codigo());
        }
        mapper.actualizar(servicio, r);
        servicios.flush();
        log.info("Servicio actualizado: id={}", id);
        return mapper.servicio(servicio);
    }

    @Override
    @Transactional
    public ServicioResponseDTO cambiarEstado(Long id, EstadoServicioRequestDTO r) {
        Servicio servicio = obtener(id);
        servicio.setActivo(r.activo());
        log.info("Estado de servicio actualizado: id={}, activo={}", id, r.activo());
        return mapper.servicio(servicio);
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        Servicio servicio = obtener(id);
        if (detalles.existsByServicioId(id)) {
            throw new ReglaNegocioException("El servicio tiene órdenes asociadas. Use PATCH /api/v1/servicios/"
                    + id + "/estado para darlo de baja");
        }
        servicios.delete(servicio);
        servicios.flush();
        log.info("Servicio eliminado: id={}", id);
    }

    private Servicio obtener(Long id) {
        return servicios.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio no encontrado: " + id));
    }
}

