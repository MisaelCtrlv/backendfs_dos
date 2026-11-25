package cl.maestros.catalogo.model;

import java.util.Arrays;
import java.util.Optional;

// Estados del pedido (mismos textos que el CHECK de la tabla pedido).
// Avanzan en orden: Pendiente -> En preparacion -> Listo -> Entregado. Anulado es aparte.
public enum EstadoPedido {
    PENDIENTE("Pendiente"),
    EN_PREPARACION("En preparación"),
    LISTO("Listo para retiro o despacho"),
    ENTREGADO("Entregado"),
    ANULADO("Anulado");

    private final String texto;

    EstadoPedido(String texto) {
        this.texto = texto;
    }

    public String getTexto() {
        return texto;
    }

    // El estado al que puede avanzar; vacio si ya termino
    public Optional<EstadoPedido> siguiente() {
        return switch (this) {
            case PENDIENTE -> Optional.of(EN_PREPARACION);
            case EN_PREPARACION -> Optional.of(LISTO);
            case LISTO -> Optional.of(ENTREGADO);
            case ENTREGADO, ANULADO -> Optional.empty();
        };
    }

    public static Optional<EstadoPedido> desdeTexto(String texto) {
        return Arrays.stream(values()).filter(e -> e.texto.equalsIgnoreCase(texto.trim())).findFirst();
    }
}
