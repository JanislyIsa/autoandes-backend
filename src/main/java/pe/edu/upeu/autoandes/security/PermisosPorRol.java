package pe.edu.upeu.autoandes.security;

import java.util.*;
import pe.edu.upeu.autoandes.entity.Rol;

public final class PermisosPorRol {
    private static final Map<String, Set<Permiso>> MATRIZ = Map.of(
            "ADMIN", Collections.unmodifiableSet(EnumSet.allOf(Permiso.class)),
            "ASESOR", Set.of(Permiso.SERVICIO_LEER, Permiso.VEHICULO_LEER, Permiso.VEHICULO_ESCRIBIR,
                    Permiso.ORDEN_LEER, Permiso.ORDEN_REGISTRAR),
            "JEFE_TALLER", Set.of(Permiso.SERVICIO_LEER, Permiso.SERVICIO_ESCRIBIR, Permiso.VEHICULO_LEER,
                    Permiso.ORDEN_LEER, Permiso.ORDEN_ANULAR, Permiso.REPORTE_VER));

    private PermisosPorRol() {}

    public static List<String> permisos(Collection<Rol> roles) {
        return roles.stream().flatMap(r -> MATRIZ.getOrDefault(r.getNombre(), Set.of()).stream())
                .map(Enum::name).distinct().sorted().toList();
    }
}

