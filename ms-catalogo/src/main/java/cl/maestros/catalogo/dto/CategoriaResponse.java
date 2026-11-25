package cl.maestros.catalogo.dto;

import cl.maestros.catalogo.model.Categoria;

public record CategoriaResponse(Short id, String nombre) {

    public static CategoriaResponse desde(Categoria c) {
        return new CategoriaResponse(c.getId(), c.getNombre());
    }
}
