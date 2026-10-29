# AutoAndes API

Spring Boot 4, Java 21, Oracle, Flyway y JWT.

## Ejecutar

Oracle: `localhost:1522/FREEPDB1`, usuario `AUTOANDESDB`. Se conserva la contraseña de prueba local.
En una copia nueva configura `ORACLE_PASSWORD` o `config/application-local.properties`.

```powershell
.\scripts\ejecutar.ps1
```

El script localiza el JDK 21. Alternativa: `mvnw.cmd spring-boot:run` con JAVA_HOME configurado.
Swagger: [localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html).
Flyway aplica la migración y Hibernate valida el esquema.

## Usuarios

Todos usan `Auto2026*`: `admin` (ADMIN), `asesor` (ASESOR), `jefe` (JEFE_TALLER).
`inactivo` usa la misma contraseña y el backend rechaza su acceso.

## Contrato de API

Base `/api/v1`. Token Bearer obligatorio salvo login, Swagger y OPTIONS.

| Método | Ruta |
| --- | --- |
| POST | /auth/login |
| GET | /auth/me |
| GET | /clientes |
| GET, POST | /servicios |
| GET, PUT, DELETE | /servicios/{id} |
| PATCH | /servicios/{id}/estado |
| GET, POST | /vehiculos?clienteId |
| GET, PUT, DELETE | /vehiculos/{id} |
| POST, GET | /ordenes?placa&estado&desde&hasta |
| GET | /ordenes/{id} |
| PATCH | /ordenes/{id}/anular |
| GET | /reportes/ingresos-por-servicio?desde&hasta |

POST: 201. Consulta/cambio: 200. DELETE: 204.
Errores: 400 validación, 401 autenticación, 403 permiso, 404 inexistente, 409 conflicto y 500 inesperado.
Formato: timestamp, status, error, mensaje, ruta; campos solo en validación.
Fechas: yyyy-MM-dd, con hasta incluido.

## Pruebas y entrega

```powershell
.\scripts\ejecutar.ps1 -Pruebas
```

Colección: `postman/AutoAndes.postman_collection.json`.
[Guía técnica](docs/guia-tecnica.md) | [Guía de defensa](docs/defensa.md) |
[Frontend](https://github.com/JanislyIsa/autoandes-frontend).

Ramas main, develop y examen-a-suarez. Etiqueta: v1.0-examen-u2.
