package cl.maestros.catalogo.dto;

import cl.maestros.catalogo.model.Producto;

public record ProductoResponse(
        String codigo,
        String nombre,
        String marca,
        String unidad,
        Integer precioVenta,
        Integer precioOferta,
        boolean oferta,
        int precioFinal,
        Integer stock,
        Integer stockMinimo,
        boolean stockCritico,
        boolean destacado,
        Short categoriaId,
        String categoria) {

    public static ProductoResponse desde(Producto p) {
        return new ProductoResponse(p.getCodigo(), p.getNombre(), p.getMarca(), p.getUnidad(),
                p.getPrecioVenta(), p.getPrecioOferta(), p.getPrecioOferta() != null, p.precioFinal(),
                p.getStock(), p.getStockMinimo(), p.getStock() <= p.getStockMinimo(), p.isDestacado(),
                p.getCategoria().getId(), p.getCategoria().getNombre());
    }
}
