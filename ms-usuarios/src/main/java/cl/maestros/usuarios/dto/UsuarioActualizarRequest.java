package cl.maestros.usuarios.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// rol y activo son opcionales y solo un administrador puede cambiarlos
public record UsuarioActualizarRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres.")
        String apellidos,

        @Size(max = 150, message = "La direccion admite hasta 150 caracteres.")
        String direccion,

        String rol,

        Boolean activo) {
}
