package cl.maestros.usuarios.dto;

import cl.maestros.usuarios.model.Rol;

public record RolResponse(Short id, String nombre) {

    public static RolResponse desde(Rol r) {
        return new RolResponse(r.getId(), r.getNombre());
    }
}
