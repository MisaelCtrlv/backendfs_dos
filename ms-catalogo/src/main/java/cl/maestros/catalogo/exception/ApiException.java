package cl.maestros.catalogo.exception;

import java.util.List;
import org.springframework.http.HttpStatus;

// Error de negocio con su codigo HTTP y, si corresponde, detalles. Lo traduce GlobalExceptionHandler.
public class ApiException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> detalles;

    public ApiException(HttpStatus status, String mensaje) {
        this(status, mensaje, List.of());
    }

    public ApiException(HttpStatus status, String mensaje, List<String> detalles) {
        super(mensaje);
        this.status = status;
        this.detalles = detalles;
    }

    public HttpStatus getStatus() { return status; }
    public List<String> getDetalles() { return detalles; }

    public static ApiException noEncontrado(String mensaje) { return new ApiException(HttpStatus.NOT_FOUND, mensaje); }
    public static ApiException conflicto(String mensaje) { return new ApiException(HttpStatus.CONFLICT, mensaje); }
    public static ApiException prohibido(String mensaje) { return new ApiException(HttpStatus.FORBIDDEN, mensaje); }
    public static ApiException invalido(String mensaje) { return new ApiException(HttpStatus.BAD_REQUEST, mensaje); }
}
