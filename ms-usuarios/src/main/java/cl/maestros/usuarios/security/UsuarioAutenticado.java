package cl.maestros.usuarios.security;

// Lo que sabemos del usuario a partir de su token (no consulta la base)
public record UsuarioAutenticado(Integer id, String correo, String rol) {

    public boolean esAdmin() {
        return "admin".equals(rol);
    }
}
