package cl.maestros.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

// Lo que envia el cliente. NO trae precios, total, usuario ni condicion de pago:
// todo eso lo calcula el servidor.
public record PedidoRequest(
        @NotEmpty(message = "El pedido debe tener al menos un producto.")
        List<@Valid PedidoItemRequest> items,

        @NotBlank(message = "El tipo de entrega es obligatorio.")
        @Pattern(regexp = "^(retiro|despacho)$", message = "El tipo de entrega debe ser retiro o despacho.")
        String tipoEntrega,

        @Size(max = 150, message = "La direccion admite hasta 150 caracteres.")
        String direccionDespacho) {
}
