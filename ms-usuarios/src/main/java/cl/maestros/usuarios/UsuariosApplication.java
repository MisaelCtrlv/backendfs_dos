package cl.maestros.usuarios;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Se excluye el usuario en memoria que Spring Security crea por defecto:
// la autenticacion es propia, con la tabla usuarios y JWT.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class UsuariosApplication {

    public static void main(String[] args) {
        SpringApplication.run(UsuariosApplication.class, args);
    }
}
