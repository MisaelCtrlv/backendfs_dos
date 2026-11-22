package cl.maestros.usuarios.config;

import cl.maestros.usuarios.model.Usuario;
import cl.maestros.usuarios.repository.RolRepository;
import cl.maestros.usuarios.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// Crea los usuarios de prueba (uno por rol) si todavia no existen.
// Se hace aqui y no en SQL porque la contrasena debe guardarse con BCrypt.
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true")
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public DataSeeder(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Override
    public void run(String... args) {
        crearSiNoExiste("Administrador", "Los Maestros", "admin@maestros.cl", "Admin123!", "admin");
        crearSiNoExiste("Valentina", "Rojas Pinto", "vendedor@maestros.cl", "Vendedor123!", "vendedor");
        crearSiNoExiste("Carlos", "Maturana Diaz", "contratista@maestros.cl", "Contratista123!", "contratista");
        crearSiNoExiste("Camila", "Soto Reyes", "cliente@maestros.cl", "Cliente123!", "cliente");
    }

    private void crearSiNoExiste(String nombre, String apellidos, String correo, String clave, String rol) {
        if (usuarios.existsByCorreo(correo)) {
            return;
        }
        Usuario u = new Usuario();
        u.setNombre(nombre);
        u.setApellidos(apellidos);
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(clave));
        u.setRol(roles.findByNombre(rol)
                .orElseThrow(() -> new IllegalStateException("Falta el rol '" + rol + "'. Ejecuta sql/02_datos.sql.")));
        usuarios.save(u);
        log.info("Usuario de prueba creado: {} ({})", correo, rol);
    }
}
