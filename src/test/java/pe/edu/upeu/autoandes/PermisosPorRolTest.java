package pe.edu.upeu.autoandes;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import pe.edu.upeu.autoandes.entity.Rol;
import pe.edu.upeu.autoandes.security.PermisosPorRol;

class PermisosPorRolTest {
    private Rol rol(String nombre) {
        var rol = new Rol();
        rol.setNombre(nombre);
        return rol;
    }

    @Test
    void variosRolesRecibenUnionSinDuplicadosYRolDesconocidoNoConcedePermisos() {
        assertThat(PermisosPorRol.permisos(List.of(rol("ASESOR"), rol("JEFE_TALLER"))))
                .containsExactly("ORDEN_ANULAR", "ORDEN_LEER", "ORDEN_REGISTRAR", "REPORTE_VER", "SERVICIO_ESCRIBIR",
                        "SERVICIO_LEER", "VEHICULO_ESCRIBIR", "VEHICULO_LEER");
        assertThat(PermisosPorRol.permisos(List.of(rol("DESCONOCIDO")))).isEmpty();
    }
}
