# Guía breve de defensa - AutoAndes

1. **Recorrido de una orden:** orden-form.ts -> OrdenService Angular -> interceptores -> JwtAuthenticationFilter -> SecurityConfig -> OrdenController -> OrdenServiceImpl -> repositorio Oracle -> AutoAndesMapper -> respuesta.
2. **Total:** el Request solo contiene vehículo, kilometraje, servicio y cantidad. El backend toma precios de la base y calcula con BigDecimal. La SPA muestra una estimación.
3. **Transacción:** @Transactional está en OrdenServiceImpl.registrar. Cabecera y detalles se confirman juntos; una excepción revierte las escrituras. Demuestra el tercer detalle inactivo comparando conteos.
4. **Reporte:** OrdenServicioRepository.ingresosPorServicio usa JOIN, SUM y GROUP BY, devuelve una proyección y solo incluye REGISTRADA. No agrega findAll con bucles.
5. **DTO:** separa el contrato de las entidades y evita relaciones anidadas y ciclos. VehiculoResponseDTO devuelve clienteId y clienteNombre. El examen no exige Lombok.
6. **JWT:** sub identifica al usuario, iat indica emisión, exp indica expiración. Está firmado con HMAC; el filtro valida firma y vencimiento en cada petición.
7. **Permisos:** PermisosPorRol define la unión por usuario; SecurityConfig usa hasAuthority y el reporte @PreAuthorize. Los controladores no comparan nombres de rol.
8. **401/403:** 401 elimina la sesión y vuelve al login con returnUrl, excepto el propio login. Un 403 conserva la sesión porque falta permiso, no autenticación.
9. **Authorization:** se agrega en auth.interceptor.ts únicamente al origen y la raíz de la API.
10. **sessionStorage:** conserva la sesión por pestaña para los equipos compartidos. AuthService programa el vencimiento y borra la sesión al salir.
11. **Ruta escrita a mano:** permisoGuard revisa el permiso declarado en data. JEFE_TALLER recibe Acceso denegado en /ordenes/nueva sin cerrar sesión.
12. **Menú reactivo:** signals, computed y siPermiso derivan las opciones de los permisos de la sesión y se actualizan al cambiar de usuario.

## Demostración sugerida

- ADMIN: registrar/editar servicio y vehículo; provocar placa duplicada.
- ASESOR: orden con dos líneas; rechazar un kilometraje menor y corregirlo conservando detalles.
- JEFE_TALLER: /ordenes/nueva denegada; anular la orden del día y revisar el reporte.
- Reporte con token ASESOR: 403. Token alterado: 401.
- Mostrar cada archivo implicado y explicar por qué la responsabilidad pertenece a esa capa.

## Verificación reproducible

Con el backend iniciado, ejecutar en el frontend: npm run test:integracion.
Usa AuthService e interceptores reales, HTTP real y almacenamiento/Router controlados en Node.
Comprueba token alterado, conservación de sesión ante 403 y vencimiento con reloj de prueba adelantado.
No equivale a una grabación manual del navegador.
