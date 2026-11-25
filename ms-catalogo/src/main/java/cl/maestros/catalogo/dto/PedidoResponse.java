package cl.maestros.catalogo.dto;

import cl.maestros.catalogo.model.Pedido;
import java.time.OffsetDateTime;
import java.util.List;

public record PedidoResponse(
        Integer id,
        String numero,
        OffsetDateTime fecha,
        String estado,
        String tipoEntrega,
        String direccionDespacho,
        String condicionPago,
        Integer total,
        Integer idUsuario,
        List<DetalleResponse> items) {

    public static PedidoResponse desde(Pedido p) {
        return new PedidoResponse(p.getId(), p.getNumero(), p.getFecha(), p.getEstado(), p.getTipoEntrega(),
                p.getDireccionDespacho(), p.getCondicionPago(), p.getTotal(), p.getIdUsuario(),
                p.getDetalles().stream().map(DetalleResponse::desde).toList());
    }
}
