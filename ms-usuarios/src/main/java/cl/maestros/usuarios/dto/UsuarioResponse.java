package cl.maestros.usuarios.dto;

import cl.maestros.usuarios.model.Usuario;

// DTO de salida: nunca incluye el hash de la contrasena
public record UsuarioResponse(
        Integer id,
        String nombre,
        String apellidos,
        String correo,
        String direccion,
        String rol,
        boolean activo) {

    public static UsuarioResponse desde(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNombre(), u.getApellidos(), u.getCorreo(),
                u.getDireccion(), u.getRol().getNombre(), u.isActivo());
    }
}
