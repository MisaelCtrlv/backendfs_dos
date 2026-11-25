package cl.maestros.catalogo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Valida los JWT emitidos por ms-usuarios. Usa la misma JWT_SECRET y exige el mismo iss y aud.
@Service
public class JwtService {

    private final SecretKey clave;
    private final String issuer;
    private final String audience;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.audience}") String audience) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
    }

    // Lanza una excepcion si la firma no calza, si vencio o si iss/aud no corresponden
    public UsuarioAutenticado validar(String token) {
        Claims c = Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(issuer)
                .requireAudience(audience)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        Integer id = ((Number) c.get("id")).intValue();
        return new UsuarioAutenticado(id, c.getSubject(), c.get("rol", String.class));
    }
}
