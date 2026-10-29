package pe.edu.upeu.autoandes;

import java.net.URI;
import java.net.http.*;
import java.time.*;
import java.util.List;
import org.junit.jupiter.api.*;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import pe.edu.upeu.autoandes.config.JwtService;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AutoAndesApiIntegrationTest {
    @LocalServerPort int puerto;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;
    private final HttpClient http = HttpClient.newHttpClient();

    private HttpResponse<String> pedir(String metodo, String ruta, String cuerpo, String token) throws Exception {
        var req = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + ruta))
                .header("Content-Type", "application/json");
        if (token != null) req.header("Authorization", "Bearer " + token);
        return http.send(req.method(metodo, cuerpo == null ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(cuerpo)).build(), HttpResponse.BodyHandlers.ofString());
    }

    private String login(String usuario) throws Exception {
        var res = pedir("POST", "/api/v1/auth/login", "{\"username\":\"" + usuario + "\",\"password\":\"Auto2026*\"}", null);
        assertThat(res.statusCode()).as(res.body()).isEqualTo(200);
        return json.readTree(res.body()).get("token").asText();
    }

    private JsonNode cuerpo(HttpResponse<String> res, int esperado) throws Exception {
        assertThat(res.statusCode()).as(res.body()).isEqualTo(esperado);
        return json.readTree(res.body());
    }

    @Test
    void loginYMeNoExponenPasswordYDevuelvenPermisos() throws Exception {
        String token = login("admin");
        var usuario = cuerpo(pedir("GET", "/api/v1/auth/me", null, token), 200);
        assertThat(usuario.get("username").asText()).isEqualTo("admin");
        assertThat(usuario.get("permisos").size()).isEqualTo(8);
        assertThat(usuario.has("password")).isFalse();
    }

    @Test
    void usuarioInactivoIncorrectoEInexistenteTienenMismo401() throws Exception {
        String mensaje = null;
        for (String usuario : List.of("inactivo", "no-existe", "admin")) {
            var error = cuerpo(pedir("POST", "/api/v1/auth/login",
                    "{\"username\":\"" + usuario + "\",\"password\":\"incorrecta\"}", null), 401);
            if (mensaje == null) mensaje = error.get("mensaje").asText();
            assertThat(error.get("mensaje").asText()).isEqualTo(mensaje);
            assertThat(error.has("campos")).isFalse();
        }
        cuerpo(pedir("POST", "/api/v1/auth/login", "{\"username\":\"inactivo\",\"password\":\"Auto2026*\"}", null), 401);
    }

    @Test
    void sinTokenAlteradoYVencidoResponden401Comun() throws Exception {
        var sinToken = cuerpo(pedir("GET", "/api/v1/clientes", null, null), 401);
        assertThat(sinToken.get("ruta").asText()).isEqualTo("/api/v1/clientes");
        assertThat(sinToken.has("timestamp")).isTrue();
        assertThat(sinToken.has("campos")).isFalse();
        String token = login("admin");
        String alterado = (token.charAt(0) == 'a' ? "b" : "a") + token.substring(1);
        cuerpo(pedir("GET", "/api/v1/clientes", null, alterado), 401);
        var antiguo = new JwtService("YXV0b2FuZGVzLXBydWViYXMtY2xhdmUtMzItYnl0ZXMtbWluaW1v", 3600,
                Clock.fixed(Instant.now().minusSeconds(7200), ZoneId.of("America/Lima")));
        String vencido = antiguo.generar(new UsuarioPrincipal(1L, "admin", "", "Admin", true, List.of("ADMIN"), List.of()));
        cuerpo(pedir("GET", "/api/v1/clientes", null, vencido), 401);
    }

    @Test
    void matrizDePermisosRechazaAccionesAunqueTokenSeaValido() throws Exception {
        String asesor = login("asesor");
        cuerpo(pedir("GET", "/api/v1/reportes/ingresos-por-servicio", null, asesor), 403);
        cuerpo(pedir("PATCH", "/api/v1/ordenes/1/anular", "{\"motivo\":\"Solicitud del cliente\"}", asesor), 403);
        cuerpo(pedir("POST", "/api/v1/servicios", "{}", asesor), 403);
        String jefe = login("jefe");
        cuerpo(pedir("POST", "/api/v1/ordenes", "{}", jefe), 403);
        cuerpo(pedir("POST", "/api/v1/vehiculos", "{}", jefe), 403);
        cuerpo(pedir("GET", "/api/v1/auth/me", null, jefe), 200);
    }

    @Test
    void crudVehiculoRelacionValidacionDuplicadoY404() throws Exception {
        String token = login("asesor");
        String request = "{\"placa\":\"TST-901\",\"marca\":\"Toyota\",\"modelo\":\"Yaris\",\"anio\":2020,\"clienteId\":1}";
        var creado = cuerpo(pedir("POST", "/api/v1/vehiculos", request, token), 201);
        long id = creado.get("id").asLong();
        assertThat(creado.get("clienteNombre").asText()).isEqualTo("Ana Torres");
        assertThat(creado.has("cliente")).isFalse();
        cuerpo(pedir("POST", "/api/v1/vehiculos", request, token), 409);
        cuerpo(pedir("GET", "/api/v1/vehiculos/999999", null, token), 404);
        cuerpo(pedir("PUT", "/api/v1/vehiculos/" + id, request.replace("Yaris", "Corolla"), token), 200);
        assertThat(pedir("DELETE", "/api/v1/vehiculos/" + id, null, token).statusCode()).isEqualTo(204);
        cuerpo(pedir("DELETE", "/api/v1/vehiculos/1", null, token), 409);
        var error = cuerpo(pedir("POST", "/api/v1/vehiculos", request.replace("TST-901", "INVALIDA"), token), 400);
        assertThat(error.get("campos").has("placa")).isTrue();
        cuerpo(pedir("POST", "/api/v1/vehiculos", request.replace("TST-901", "TST-902").replace("\"clienteId\":1", "\"clienteId\":999999"), token), 404);
    }

    @Test
    void crudServicioBajaLogicaYBorradoProtegido() throws Exception {
        String token = login("jefe");
        String request = "{\"codigo\":\"SRV-901\",\"nombre\":\"Servicio de prueba\",\"precio\":30,\"duracionMin\":30,\"activo\":true}";
        long id = cuerpo(pedir("POST", "/api/v1/servicios", request, token), 201).get("id").asLong();
        cuerpo(pedir("POST", "/api/v1/servicios", request, token), 409);
        cuerpo(pedir("PUT", "/api/v1/servicios/" + id, request.replace("prueba", "edición"), token), 200);
        var baja = cuerpo(pedir("PATCH", "/api/v1/servicios/" + id + "/estado", "{\"activo\":false}", token), 200);
        assertThat(baja.get("activo").asBoolean()).isFalse();
        assertThat(pedir("DELETE", "/api/v1/servicios/" + id, null, token).statusCode()).isEqualTo(204);
        cuerpo(pedir("DELETE", "/api/v1/servicios/1", null, token), 409);
        cuerpo(pedir("POST", "/api/v1/servicios", request.replace("SRV-901", "MAL"), token), 400);
    }

    private String orden(int km, String detalles) {
        return "{\"vehiculoId\":1,\"kilometraje\":" + km + ",\"detalles\":[" + detalles + "]}";
    }

    @Test
    void tercerDetalleInactivoNoDejaCabeceraNiDetallesYValidaKm() throws Exception {
        String token = login("asesor");
        Long cabeceras = jdbc.queryForObject("select count(*) from orden_servicio", Long.class);
        Long detalles = jdbc.queryForObject("select count(*) from detalle_orden", Long.class);
        String items = "{\"servicioId\":1,\"cantidad\":1},{\"servicioId\":2,\"cantidad\":1},{\"servicioId\":8,\"cantidad\":1}";
        cuerpo(pedir("POST", "/api/v1/ordenes", orden(9999999, items), token), 409);
        assertThat(jdbc.queryForObject("select count(*) from orden_servicio", Long.class)).isEqualTo(cabeceras);
        assertThat(jdbc.queryForObject("select count(*) from detalle_orden", Long.class)).isEqualTo(detalles);
        var error = cuerpo(pedir("POST", "/api/v1/ordenes", orden(1, "{\"servicioId\":1,\"cantidad\":1}"), token), 409);
        assertThat(error.get("mensaje").asText()).contains("45000");
    }

    @Test
    void reglasDeDetallesYFormatosResponden400() throws Exception {
        String token = login("asesor");
        String item = "{\"servicioId\":1,\"cantidad\":1}";
        cuerpo(pedir("POST", "/api/v1/ordenes", orden(9999999, item + "," + item), token), 400);
        cuerpo(pedir("POST", "/api/v1/ordenes", orden(9999999, ""), token), 400);
        cuerpo(pedir("POST", "/api/v1/ordenes", orden(9999999, item.replace("\"cantidad\":1", "\"cantidad\":6")), token), 400);
        cuerpo(pedir("POST", "/api/v1/ordenes", orden(9999999, String.join(",", java.util.Collections.nCopies(11, item))), token), 400);
        cuerpo(pedir("GET", "/api/v1/ordenes?desde=incorrecta", null, token), 400);
        cuerpo(pedir("POST", "/api/v1/ordenes", "{", token), 400);
    }

    @Test
    void registroCalculaTotalConsultaYAnulacionExcluyeImporteDelReporte() throws Exception {
        String token = login("admin");
        var reporteAntes = cuerpo(pedir("GET", "/api/v1/reportes/ingresos-por-servicio", null, token), 200);
        var creada = cuerpo(pedir("POST", "/api/v1/ordenes",
                orden(9999999, "{\"servicioId\":1,\"cantidad\":2},{\"servicioId\":2,\"cantidad\":1}"), token), 201);
        long id = creada.get("id").asLong();
        assertThat(creada.get("numero").asText()).matches("OS-[0-9]{6}");
        assertThat(creada.get("total").decimalValue()).isEqualByComparingTo("320");
        assertThat(creada.get("usuarioRegistro").asText()).isEqualTo("admin");
        assertThat(creada.get("detalles").size()).isEqualTo(2);
        cuerpo(pedir("GET", "/api/v1/ordenes/" + id, null, token), 200);
        String hoy = LocalDate.now(ZoneId.of("America/Lima")).toString();
        var filtradas = cuerpo(pedir("GET", "/api/v1/ordenes?placa=AAA-111&estado=REGISTRADA&desde=" + hoy + "&hasta=" + hoy, null, token), 200);
        assertThat(filtradas.toString()).as("Orden creada: %s; fecha SQL: %s; hoy: %s", creada,
                jdbc.queryForObject("select fecha from orden_servicio where id = ?", java.sql.Timestamp.class, id), hoy)
                .contains(creada.get("numero").asText());
        var anulada = cuerpo(pedir("PATCH", "/api/v1/ordenes/" + id + "/anular", "{\"motivo\":\"Cliente canceló la atención\"}", token), 200);
        assertThat(anulada.get("estado").asText()).isEqualTo("ANULADA");
        cuerpo(pedir("PATCH", "/api/v1/ordenes/" + id + "/anular", "{\"motivo\":\"Cliente canceló la atención\"}", token), 409);
        var reporteDespues = cuerpo(pedir("GET", "/api/v1/reportes/ingresos-por-servicio", null, token), 200);
        assertThat(reporteDespues).isEqualTo(reporteAntes);
        cuerpo(pedir("PATCH", "/api/v1/ordenes/1/anular", "{\"motivo\":\"Cliente canceló la atención\"}", token), 409);
        cuerpo(pedir("PATCH", "/api/v1/ordenes/2/anular", "{\"motivo\":\"corto\"}", token), 400);
    }

    @Test
    void reporteAgregaEnBDYExcluyeSemillaAnulada() throws Exception {
        var reporte = cuerpo(pedir("GET", "/api/v1/reportes/ingresos-por-servicio", null, login("jefe")), 200);
        for (JsonNode fila : reporte) {
            if (fila.get("codigo").asText().equals("SRV-001")) {
                assertThat(fila.get("importeTotal").decimalValue()).isEqualByComparingTo("120");
                assertThat(fila.get("cantidadTotal").asLong()).isEqualTo(1);
            }
        }
        assertThat(reporte.get(0).get("codigo").asText()).isEqualTo("SRV-003");
    }

    @Test
    void swaggerPublicoYCorsAceptaAuthorization() throws Exception {
        var especificacion = cuerpo(pedir("GET", "/v3/api-docs", null, null), 200);
        assertThat(especificacion.get("paths").get("/api/v1/auth/login").get("post").get("security").size()).isZero();
        var preflight = HttpRequest.newBuilder(URI.create("http://localhost:" + puerto + "/api/v1/ordenes"))
                .header("Origin", "http://localhost:4200").header("Access-Control-Request-Method", "POST")
                .header("Access-Control-Request-Headers", "Authorization, Content-Type")
                .method("OPTIONS", HttpRequest.BodyPublishers.noBody()).build();
        var res = http.send(preflight, HttpResponse.BodyHandlers.ofString());
        assertThat(res.statusCode()).isEqualTo(200);
        assertThat(res.headers().firstValue("Access-Control-Allow-Origin")).contains("http://localhost:4200");
    }
}
