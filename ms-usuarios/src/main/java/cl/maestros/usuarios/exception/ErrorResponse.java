package cl.maestros.usuarios.exception;

import java.util.List;

// Forma unica de los errores: { "mensaje": "...", "detalles": [...] } (guia 20)
public record ErrorResponse(String mensaje, List<String> detalles) {

    public static ErrorResponse de(String mensaje) {
        return new ErrorResponse(mensaje, List.of());
    }
}
