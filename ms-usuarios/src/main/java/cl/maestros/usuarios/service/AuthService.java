package cl.maestros.usuarios.service;

import cl.maestros.usuarios.dto.LoginRequest;
import cl.maestros.usuarios.dto.LoginResponse;
import cl.maestros.usuarios.dto.RegistroRequest;
import cl.maestros.usuarios.dto.UsuarioResponse;
import cl.maestros.usuarios.exception.ApiException;
import cl.maestros.usuarios.model.Rol;
import cl.maestros.usuarios.model.Usuario;
import cl.maestros.usuarios.repository.RolRepository;
import cl.maestros.usuarios.repository.UsuarioRepository;
import cl.maestros.usuarios.security.JwtService;
import cl.maestros.usuarios.security.UsuarioAutenticado;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    static final String ROL_REGISTRO = "cliente";

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    // Registro publico: el rol lo asigna el servidor, nunca el cliente
    @Transactional
    public UsuarioResponse registrar(RegistroRequest r) {
        String correo = normalizarCorreo(r.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        Rol cliente = roles.findByNombre(ROL_REGISTRO)
                .orElseThrow(() -> new IllegalStateException("Falta el rol 'cliente' en la tabla rol."));
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setDireccion(textoOpcional(r.direccion()));
        u.setRol(cliente);
        return UsuarioResponse.desde(usuarios.save(u));
    }

    // Mismo mensaje si falla el correo o la clave: no revela cual de los dos fue
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest r) {
        Usuario u = usuarios.findByCorreo(normalizarCorreo(r.correo()))
                .filter(Usuario::isActivo)
                .filter(encontrado -> encoder.matches(r.password(), encontrado.getPasswordHash()))
                .orElseThrow(() -> ApiException.noAutorizado("Correo o contrasena incorrectos."));
        return new LoginResponse(jwt.generar(u), "Bearer", jwt.segundosDeVigencia(), UsuarioResponse.desde(u));
    }

    // Datos actuales del usuario dueno del token
    @Transactional(readOnly = true)
    public UsuarioResponse yo(UsuarioAutenticado actor) {
        return usuarios.findById(actor.id())
                .filter(Usuario::isActivo)
                .map(UsuarioResponse::desde)
                .orElseThrow(() -> ApiException.noAutorizado("La cuenta ya no esta disponible."));
    }

    static String normalizarCorreo(String correo) {
        return correo.trim().toLowerCase();
    }

    static String textoOpcional(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
