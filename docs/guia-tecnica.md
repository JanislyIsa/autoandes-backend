# AutoAndes API

Backend del caso AutoAndes con Spring Boot 4, Java 21, Oracle, Flyway y JWT.
La organización de controladores, servicios, DTO, repositorios y seguridad sigue como referencia
el proyecto propio [pharmaSoft](https://github.com/JanislyIsa/pharmaSoft).

## 1. Preparar Oracle

El perfil `dev`, activo por defecto, usa `jdbc:oracle:thin:@//localhost:1522/FREEPDB1`,
usuario `AUTOANDESDB`. La contraseña local está en `config/application-local.properties`, excluido de Git.
El contenedor existente es `oracle-local`. Para crear el usuario por primera vez:

```powershell
docker start oracle-local
docker cp .\scripts\crear_usuario_oracle.sql oracle-local:/tmp/autoandes-crear-usuario.sql
docker exec -it -u oracle oracle-local sqlplus -s / as sysdba @/tmp/autoandes-crear-usuario.sql
```

El script solicita la contraseña y crea únicamente el usuario de AutoAndes si no existe. Flyway crea sus tablas y datos
al arrancar la aplicación. No se requiere ejecutar manualmente la migración ni usar `ddl-auto=update`.
No actives `baseline-on-migrate` para saltarte la migración inicial en un esquema vacío.

Puedes sustituir la configuración con `ORACLE_URL`, `ORACLE_USERNAME` y `ORACLE_PASSWORD`.
En una nueva copia del repositorio, define `ORACLE_PASSWORD` antes de arrancar o crea el archivo local
ignorado con `spring.datasource.password=<tu contraseña>`. La contraseña no se incluye en los commits.
`FRONTEND_ORIGIN` permite cambiar el origen CORS de `http://localhost:4200`.

## 2. Ejecutar

Desde PowerShell, con Oracle iniciado:

```powershell
.\scripts\ejecutar.ps1
```

El script busca un JDK 21 en las instalaciones locales si no está definido `JAVA_HOME`.
También puedes configurar `JAVA_HOME` y ejecutar `./mvnw.cmd spring-boot:run` directamente.
Swagger: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
OpenAPI: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs).

La clave JWT local es temporal: reiniciar el backend invalida los tokens anteriores.
Para conservarla entre reinicios, define `JWT_SECRET` con una clave aleatoria Base64 de al menos 32 bytes.
El token dura 3600 segundos; cada petición valida firma, expiración, usuario activo y permisos.

## 3. Datos de prueba

Todos los usuarios usan `Auto2026*`. Las contraseñas de la migración están cifradas con BCrypt.

| Usuario | Rol | Estado |
| --- | --- | --- |
| admin | ADMIN | Activo |
| asesor | ASESOR | Activo |
| jefe | JEFE_TALLER | Activo |
| inactivo | ASESOR | Inactivo |

La migración incluye seis clientes, ocho servicios (`SRV-008` inactivo), ocho vehículos,
tres órdenes registradas y una anulada. `AAA-111` tiene una orden previa de 45000 km.
Las órdenes semilla toman su fecha del momento en que se ejecuta la migración.

## 4. Contrato HTTP

Base: `/api/v1`. Todas las operaciones, salvo login, requieren `Authorization: Bearer <token>`.
Swagger y las solicitudes OPTIONS también son públicas.

| Método | Ruta | Permiso |
| --- | --- | --- |
| POST | /auth/login | Público |
| GET | /auth/me | Autenticado |
| GET | /clientes | VEHICULO_LEER |
| GET | /servicios y /servicios/{id} | SERVICIO_LEER |
| POST, PUT, DELETE | /servicios, /servicios/{id} según el verbo | SERVICIO_ESCRIBIR |
| PATCH | /servicios/{id}/estado | SERVICIO_ESCRIBIR |
| GET | /vehiculos?clienteId y /vehiculos/{id} | VEHICULO_LEER |
| POST, PUT, DELETE | /vehiculos, /vehiculos/{id} según el verbo | VEHICULO_ESCRIBIR |
| POST | /ordenes | ORDEN_REGISTRAR |
| GET | /ordenes?placa&estado&desde&hasta y /ordenes/{id} | ORDEN_LEER |
| PATCH | /ordenes/{id}/anular | ORDEN_ANULAR |
| GET | /reportes/ingresos-por-servicio?desde&hasta | REPORTE_VER |

POST crea con 201; consultas, cambios y anulación responden 200; DELETE exitoso responde 204.
Validación: 400. Recurso inexistente: 404. Duplicados y conflictos de negocio: 409.
Sin autenticación o con token inválido/vencido: 401. Sin permiso: 403.
Las listas son arrays JSON; los filtros son opcionales, combinables y usan fechas ISO `yyyy-MM-dd`.
`hasta` incluye el día completo. Sin coincidencias se devuelve `[]` con 200.

Login:

```json
{"username":"asesor","password":"Auto2026*"}
```

Servicio:

```json
{"codigo":"SRV-009","nombre":"Inspección general","precio":100.00,"duracionMin":60,"activo":true}
```

Vehículo:

```json
{"placa":"ABC-123","marca":"Toyota","modelo":"Yaris","anio":2020,"clienteId":1}
```

Orden:

```json
{"vehiculoId":1,"kilometraje":46000,"detalles":[{"servicioId":1,"cantidad":2},{"servicioId":2,"cantidad":1}]}
```

El backend obtiene precios de Oracle y calcula subtotales y total. La petición no acepta un total
como fuente de cálculo. La respuesta incluye la orden completa y su número `OS-000005` o siguiente.
La numeración usa una secuencia Oracle: es única bajo concurrencia y puede tener saltos al fallar una transacción.

Anulación: `{"motivo":"Cliente canceló la atención"}`. Cambio de estado de servicio: `{"activo":false}`.
Los errores tienen `timestamp`, `status`, `error`, `mensaje` y `ruta`; `campos` aparece solo en validación.

## 5. Recorrido del código y reglas

1. `controller`: recibe y valida los DTO, llama al servicio y establece el código HTTP.
2. `service` y `service/impl`: contienen RN-01 a RN-05 y las transacciones.
3. `repository`: consulta y persiste con JPA; el reporte usa JPQL con GROUP BY y proyección.
4. `mapper`: transforma las entidades en DTO dentro de la transacción.
5. `exception`: convierte errores al contrato común.
6. `security/PermisosPorRol`: define la unión de permisos por rol. `config/SecurityConfig` autoriza por permiso.

Un servicio usado no se elimina: DELETE devuelve 409 y permite desactivarlo con PATCH.
Un vehículo con órdenes no se elimina. No se repiten servicios en una orden; hay entre 1 y 10
líneas, con cantidades de 1 a 5. Los servicios deben estar activos. El vehículo se bloquea durante
el registro para comprobar el último kilometraje confirmado. Solo se anula una orden registrada
el mismo día, según `America/Lima`, con motivo de 10 a 200 caracteres.

## 6. Verificación y Postman

```powershell
.\scripts\ejecutar.ps1 -Pruebas
```

Las pruebas arrancan el backend real en un puerto temporal y ejecutan la migración en H2 en modo
Oracle, únicamente para pruebas. Cubren CRUD, validación, reglas de órdenes, ausencia de registros
parciales, reporte, autenticación, permisos, expiración, Swagger y CORS. Esta prueba no sustituye
la comprobación del esquema y las operaciones contra Oracle.

Importa `postman/AutoAndes.postman_collection.json` en Postman. El orden de ejecución obtiene los
tres tokens y conserva los identificadores creados. Incluye ejemplos de éxito, 400, 401, 403, 404 y 409.
La fuente del contrato es el enunciado del examen; OpenAPI se genera desde los controladores y DTO.

Validación realizada el 29 de octubre de 2026: 13 pruebas correctas con H2 y las mismas 13
contra Oracle, con validación del esquema activada. También se ejecutaron las 35 solicitudes
de la colección contra el backend normal conectado a Oracle, comprobando códigos, formato de
errores, total calculado y filtros; sus respuestas están guardadas como ejemplos sin tokens activos.
Esta comprobación de las solicitudes se hizo con un cliente HTTP local; los scripts `pm.test`
quedan incluidos para ejecutar la colección posteriormente en Postman.

## 7. Entrega pendiente

El frontend, el PDF de capturas y la defensa se realizan después de validar este backend.
El backend se desarrolla por bloques en `examen-a-suarez` y se integra en `develop` y `main`
para su publicación. La etiqueta `v1.0-examen-u2` se reserva para el commit de la entrega final.
