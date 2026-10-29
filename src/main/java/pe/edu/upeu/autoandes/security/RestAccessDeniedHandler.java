package pe.edu.upeu.autoandes.security;

import java.io.IOException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAccessDeniedHandler implements AccessDeniedHandler {
    private final RespuestaErrorSeguridad errores;
    @Override
    public void handle(HttpServletRequest req, HttpServletResponse res, AccessDeniedException ex) throws IOException {
        errores.escribir(req, res, HttpStatus.FORBIDDEN, "No tiene permisos para realizar esta operación");
    }
}

