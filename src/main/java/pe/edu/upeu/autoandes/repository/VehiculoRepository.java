package pe.edu.upeu.autoandes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pe.edu.upeu.autoandes.entity.Vehiculo;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Long> {
    boolean existsByPlaca(String placa);
    boolean existsByPlacaAndIdNot(String placa, Long id);
    java.util.List<Vehiculo> findByClienteIdOrderByPlacaAsc(Long clienteId);
    java.util.List<Vehiculo> findAllByOrderByPlacaAsc();

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select v from Vehiculo v where v.id = :id")
    java.util.Optional<Vehiculo> buscarParaRegistro(Long id);
}

