package cl.maestros.catalogo.security;

// Datos del usuario leidos del token que emitio ms-usuarios (no se consulta su tabla)
public record UsuarioAutenticado(Integer id, String correo, String rol) {

    public boolean esAdmin() {
        return "admin".equals(rol);
    }

    // Personal de la ferreteria: ve todos los pedidos y cambia su estado
    public boolean esPersonal() {
        return "admin".equals(rol) || "vendedor".equals(rol);
    }

    public boolean esContratista() {
        return "contratista".equals(rol);
    }
}
