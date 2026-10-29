package pe.edu.upeu.autoandes.config;

import java.time.Clock;
import java.util.Date;
import javax.crypto.SecretKey;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.upeu.autoandes.security.UsuarioPrincipal;

@Service
@Slf4j
public class JwtService {
    private final SecretKey clave;
    private final long expiracion;
    private final Clock reloj;

    public JwtService(@Value("${app.jwt.secret}") String secreto,
            @Value("${app.jwt.expiracion-segundos}") long expiracion, Clock reloj) {
        this.clave = secreto.isBlank() ? Jwts.SIG.HS256.key().build() : Keys.hmacShaKeyFor(Decoders.BASE64.decode(secreto));
        this.expiracion = expiracion;
        this.reloj = reloj;
        if (secreto.isBlank()) log.info("JWT usa una clave temporal de desarrollo; las sesiones vencen al reiniciar");
    }

    public String generar(UsuarioPrincipal usuario) {
        var ahora = reloj.instant();
        return Jwts.builder().subject(usuario.getUsername()).issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(expiracion)))
                .claim("roles", usuario.roles()).claim("permisos", usuario.permisos())
                .signWith(clave).compact();
    }

    public String validarYObtenerUsername(String token) {
        return Jwts.parser().verifyWith(clave).clock(() -> Date.from(reloj.instant())).build()
                .parseSignedClaims(token).getPayload().getSubject();
    }

    public long expiracionSegundos() { return expiracion; }
}

