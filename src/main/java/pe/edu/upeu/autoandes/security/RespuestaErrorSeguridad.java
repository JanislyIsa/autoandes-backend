package pe.edu.upeu.autoandes.security;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import pe.edu.upeu.autoandes.dto.ErrorResponseDTO;

@Component
@RequiredArgsConstructor
public class RespuestaErrorSeguridad {
    private final ObjectMapper json;
    private final Clock reloj;
    public void escribir(HttpServletRequest req, HttpServletResponse res, HttpStatus estado, String mensaje) throws IOException {
        res.setStatus(estado.value());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        json.writeValue(res.getOutputStream(), new ErrorResponseDTO(LocalDateTime.now(reloj), estado.value(),
                estado.getReasonPhrase(), mensaje, req.getRequestURI(), null));
    }
}

