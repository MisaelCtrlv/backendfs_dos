package cl.maestros.usuarios.security;

import cl.maestros.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Crea y valida los JWT. Firma HS256 con JWT_SECRET (la misma clave la usa ms-catalogo).
@Service
public class JwtService {

    private final SecretKey clave;
    private final String issuer;
    private final String audience;
    private final long minutos;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.issuer}") String issuer,
                      @Value("${app.jwt.audience}") String audience,
                      @Value("${app.jwt.expiracion-minutos}") long minutos) {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET debe tener al menos 32 caracteres.");
        }
        this.clave = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.issuer = issuer;
        this.audience = audience;
        this.minutos = minutos;
    }

    // sub = correo; id y rol para autorizar sin consultar la base; exp = vencimiento
    public String generar(Usuario u) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(u.getCorreo())
                .issuer(issuer)
                .audience().add(audience).and()
                .claim("id", u.getId())
                .claim("rol", u.getRol().getNombre())
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plusSeconds(minutos * 60)))
                .signWith(clave)
                .compact();
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

    public long segundosDeVigencia() {
        return minutos * 60;
    }
}
