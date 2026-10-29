package pe.edu.upeu.autoandes.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO;
import pe.edu.upeu.autoandes.entity.EstadoOrden;
import pe.edu.upeu.autoandes.entity.OrdenServicio;

public interface OrdenServicioRepository extends JpaRepository<OrdenServicio, Long> {
    boolean existsByVehiculoId(Long vehiculoId);
    Optional<OrdenServicio> findFirstByVehiculoIdAndEstadoOrderByFechaDescIdDesc(Long vehiculoId, EstadoOrden estado);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from OrdenServicio o where o.id = :id")
    Optional<OrdenServicio> buscarParaAnular(Long id);

    @Query(value = "SELECT orden_numero_seq.NEXTVAL FROM DUAL", nativeQuery = true)
    Long siguienteNumero();

    @Query("""
            select o from OrdenServicio o join fetch o.vehiculo v join fetch v.cliente
            where (:placa is null or upper(v.placa) like upper(concat('%', :placa, '%')) escape '!')
              and (:estado is null or o.estado = :estado)
              and (:desde is null or o.fecha >= :desde)
              and (:hasta is null or o.fecha < :hasta)
            order by o.fecha desc, o.id desc
            """)
    List<OrdenServicio> buscar(String placa, EstadoOrden estado, LocalDateTime desde, LocalDateTime hasta);

    @Query("""
            select new pe.edu.upeu.autoandes.dto.IngresoPorServicioDTO(
                s.codigo, s.nombre, sum(d.cantidad), sum(d.subtotal))
            from DetalleOrden d join d.servicio s join d.orden o
            where o.estado = pe.edu.upeu.autoandes.entity.EstadoOrden.REGISTRADA
              and (:desde is null or o.fecha >= :desde)
              and (:hasta is null or o.fecha < :hasta)
            group by s.codigo, s.nombre
            order by sum(d.subtotal) desc, s.codigo asc
            """)
    List<IngresoPorServicioDTO> ingresosPorServicio(LocalDateTime desde, LocalDateTime hasta);
}
