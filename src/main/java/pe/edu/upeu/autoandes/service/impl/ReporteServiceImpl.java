package pe.edu.upeu.autoandes.service.impl;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.repository.OrdenServicioRepository;
import pe.edu.upeu.autoandes.service.ReporteService;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {
    private final OrdenServicioRepository ordenes;
    @Override
    public List<IngresoPorServicioDTO> ingresos(LocalDate desde, LocalDate hasta) {
        OrdenServiceImpl.validarRango(desde, hasta);
        return ordenes.ingresosPorServicio(desde == null ? null : desde.atStartOfDay(),
                hasta == null ? null : hasta.plusDays(1).atStartOfDay());
    }
}
