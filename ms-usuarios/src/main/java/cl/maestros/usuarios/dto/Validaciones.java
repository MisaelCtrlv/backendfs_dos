package cl.maestros.usuarios.dto;

// Misma regla de contrasena que el frontend (validarContrasena)
public final class Validaciones {

    public static final String CLAVE_FUERTE = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,72}$";
    public static final String MENSAJE_CLAVE =
            "La contrasena debe tener al menos 8 caracteres, una minuscula, una mayuscula, un numero y un simbolo.";

    private Validaciones() {
    }
}
