package pe.edu.upeu.autoandes.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import pe.edu.upeu.autoandes.enums.EstadoOrden;

@Entity
@Table(name = "orden_servicio")
@Getter
@Setter
public class OrdenServicio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 9)
    private String numero;
    @Column(nullable = false)
    private java.time.LocalDateTime fecha;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vehiculo_id", nullable = false)
    private Vehiculo vehiculo;
    @Column(nullable = false)
    private Integer kilometraje;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private EstadoOrden estado;
    @Column(nullable = false, precision = 10, scale = 2)
    private java.math.BigDecimal total;
    @Column(name = "usuario_registro", nullable = false, length = 30)
    private String usuarioRegistro;
    @Column(name = "motivo_anulacion", length = 200)
    private String motivoAnulacion;
    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<DetalleOrden> detalles = new java.util.ArrayList<>();

    public void agregarDetalle(DetalleOrden detalle) {
        detalles.add(detalle);
        detalle.setOrden(this);
    }
}

