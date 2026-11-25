package cl.maestros.catalogo.dto;

import cl.maestros.catalogo.model.DetallePedido;

public record DetalleResponse(String codigo, String nombre, Integer cantidad, Integer precioUnitario, int subtotal) {

    public static DetalleResponse desde(DetallePedido d) {
        return new DetalleResponse(d.getProducto().getCodigo(), d.getProducto().getNombre(),
                d.getCantidad(), d.getPrecioUnitario(), d.subtotal());
    }
}
