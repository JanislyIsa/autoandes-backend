package pe.edu.upeu.autoandes.config;

import java.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import pe.edu.upeu.autoandes.security.*;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final UsuarioDetailsService usuarios;
    private final RestAuthenticationEntryPoint errores;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String ruta = req.getServletPath();
        return "OPTIONS".equals(req.getMethod()) || ruta.equals("/api/v1/auth/login")
                || ruta.equals("/swagger-ui.html") || ruta.startsWith("/swagger-ui/") || ruta.startsWith("/v3/api-docs");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String encabezado = req.getHeader("Authorization");
        if (encabezado == null) {
            chain.doFilter(req, res);
            return;
        }
        try {
            if (!encabezado.startsWith("Bearer ") || encabezado.length() <= 7) throw new BadCredentialsException("Token inválido");
            String username = jwt.validarYObtenerUsername(encabezado.substring(7));
            if (username == null || username.isBlank()) throw new BadCredentialsException("Token inválido");
            UsuarioPrincipal usuario = usuarios.loadUserByUsername(username);
            if (!usuario.isEnabled()) throw new DisabledException("Cuenta inactiva");
            var auth = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);
        } catch (JwtException | IllegalArgumentException | AuthenticationException ex) {
            SecurityContextHolder.clearContext();
            errores.commence(req, res, new BadCredentialsException("Token inválido", ex));
            return;
        }
        chain.doFilter(req, res);
    }
}

