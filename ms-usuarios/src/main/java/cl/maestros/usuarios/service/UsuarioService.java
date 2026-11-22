package cl.maestros.usuarios.service;

import cl.maestros.usuarios.dto.RolResponse;
import cl.maestros.usuarios.dto.UsuarioActualizarRequest;
import cl.maestros.usuarios.dto.UsuarioCrearRequest;
import cl.maestros.usuarios.dto.UsuarioResponse;
import cl.maestros.usuarios.exception.ApiException;
import cl.maestros.usuarios.model.Rol;
import cl.maestros.usuarios.model.Usuario;
import cl.maestros.usuarios.repository.RolRepository;
import cl.maestros.usuarios.repository.UsuarioRepository;
import cl.maestros.usuarios.security.UsuarioAutenticado;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarios;
    private final RolRepository roles;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, RolRepository roles, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.roles = roles;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarios.findAllByOrderByIdAsc().stream().map(UsuarioResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return roles.findAllByOrderByIdAsc().stream().map(RolResponse::desde).toList();
    }

    // Un usuario solo ve su propia cuenta; el administrador ve cualquiera
    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Integer id, UsuarioAutenticado actor) {
        exigirPropioOAdmin(id, actor);
        return UsuarioResponse.desde(buscar(id));
    }

    // Alta hecha por el administrador (por ejemplo, un vendedor)
    @Transactional
    public UsuarioResponse crear(UsuarioCrearRequest r) {
        String correo = AuthService.normalizarCorreo(r.correo());
        if (usuarios.existsByCorreo(correo)) {
            throw ApiException.conflicto("Ya existe una cuenta con ese correo.");
        }
        Usuario u = new Usuario();
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setCorreo(correo);
        u.setPasswordHash(encoder.encode(r.password()));
        u.setDireccion(AuthService.textoOpcional(r.direccion()));
        u.setRol(buscarRol(r.rol()));
        return UsuarioResponse.desde(usuarios.save(u));
    }

    // Cada uno modifica sus datos; rol y estado solo los cambia el administrador
    @Transactional
    public UsuarioResponse actualizar(Integer id, UsuarioActualizarRequest r, UsuarioAutenticado actor) {
        exigirPropioOAdmin(id, actor);
        Usuario u = buscar(id);
        u.setNombre(r.nombre().trim());
        u.setApellidos(r.apellidos().trim());
        u.setDireccion(AuthService.textoOpcional(r.direccion()));

        if (r.rol() != null && !r.rol().equals(u.getRol().getNombre())) {
            if (!actor.esAdmin()) {
                throw ApiException.prohibido("Solo un administrador puede cambiar el rol.");
            }
            u.setRol(buscarRol(r.rol()));
        }
        if (r.activo() != null && r.activo() != u.isActivo()) {
            if (!actor.esAdmin()) {
                throw ApiException.prohibido("Solo un administrador puede activar o desactivar cuentas.");
            }
            if (!r.activo() && actor.id().equals(id)) {
                throw ApiException.conflicto("No puedes desactivar tu propia cuenta.");
            }
            u.setActivo(r.activo());
        }
        return UsuarioResponse.desde(usuarios.save(u));
    }

    // "Eliminar" = desactivar: ya no puede iniciar sesion, pero sus pedidos se conservan
    @Transactional
    public void desactivar(Integer id, UsuarioAutenticado actor) {
        if (actor.id().equals(id)) {
            throw ApiException.conflicto("No puedes desactivar tu propia cuenta.");
        }
        Usuario u = buscar(id);
        u.setActivo(false);
        usuarios.save(u);
    }

    private Usuario buscar(Integer id) {
        return usuarios.findById(id).orElseThrow(() -> ApiException.noEncontrado("Usuario no encontrado."));
    }

    private Rol buscarRol(String nombre) {
        return roles.findByNombre(nombre.trim().toLowerCase())
                .orElseThrow(() -> ApiException.invalido("El rol '" + nombre + "' no existe."));
    }

    private void exigirPropioOAdmin(Integer id, UsuarioAutenticado actor) {
        if (!actor.esAdmin() && !actor.id().equals(id)) {
            throw ApiException.prohibido("No puedes acceder a la cuenta de otra persona.");
        }
    }
}
