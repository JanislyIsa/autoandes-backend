package pe.edu.upeu.autoandes.security;

import java.io.IOException;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final RespuestaErrorSeguridad errores;
    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res, AuthenticationException ex) throws IOException {
        errores.escribir(req, res, HttpStatus.UNAUTHORIZED, "Autenticación requerida o token inválido");
    }
}

