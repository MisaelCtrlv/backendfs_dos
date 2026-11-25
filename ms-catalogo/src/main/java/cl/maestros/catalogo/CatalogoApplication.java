package cl.maestros.catalogo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

// Los usuarios viven en ms-usuarios: aqui solo se valida el JWT que ese servicio emite
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class CatalogoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogoApplication.class, args);
    }
}
