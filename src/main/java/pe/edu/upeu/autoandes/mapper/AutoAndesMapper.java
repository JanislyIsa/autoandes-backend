package pe.edu.upeu.autoandes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.autoandes.dto.*;
import pe.edu.upeu.autoandes.entity.*;

@Component
public class AutoAndesMapper {
    public ClienteResponseDTO cliente(Cliente c) {
        return new ClienteResponseDTO(c.getId(), c.getDocumento(), c.getNombres(), c.getTelefono());
    }

    public ServicioResponseDTO servicio(Servicio s) {
        return new ServicioResponseDTO(s.getId(), s.getCodigo(), s.getNombre(), s.getPrecio(), s.getDuracionMin(), s.getActivo());
    }

    public void actualizar(Servicio s, ServicioRequestDTO r) {
        s.setCodigo(r.getCodigo());
        s.setNombre(r.getNombre().trim());
        s.setPrecio(r.getPrecio());
        s.setDuracionMin(r.getDuracionMin());
        s.setActivo(r.getActivo());
    }

    public VehiculoResponseDTO vehiculo(Vehiculo v) {
        return new VehiculoResponseDTO(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAnio(),
                v.getCliente().getId(), v.getCliente().getNombres());
    }

    public void actualizar(Vehiculo v, VehiculoRequestDTO r, Cliente cliente) {
        v.setPlaca(r.placa());
        v.setMarca(r.marca().trim());
        v.setModelo(r.modelo().trim());
        v.setAnio(r.anio());
        v.setCliente(cliente);
    }

    public OrdenResponseDTO orden(OrdenServicio o) {
        var detalles = o.getDetalles().stream().map(d -> new DetalleOrdenResponseDTO(d.getId(),
                d.getServicio().getId(), d.getServicio().getCodigo(), d.getServicio().getNombre(),
                d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal())).toList();
        return new OrdenResponseDTO(o.getId(), o.getNumero(), o.getFecha(), o.getVehiculo().getId(),
                o.getVehiculo().getPlaca(), o.getVehiculo().getCliente().getNombres(), o.getKilometraje(),
                o.getEstado().name(), o.getTotal(), o.getUsuarioRegistro(), o.getMotivoAnulacion(), detalles);
    }
}
