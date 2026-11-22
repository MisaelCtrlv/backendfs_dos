package cl.maestros.usuarios.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

// Registro publico. No trae rol: el servidor asigna "cliente".
public record RegistroRequest(
        @NotBlank(message = "El nombre es obligatorio.")
        @Size(min = 2, max = 50, message = "El nombre debe tener entre 2 y 50 caracteres.")
        String nombre,

        @NotBlank(message = "Los apellidos son obligatorios.")
        @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres.")
        String apellidos,

        @NotBlank(message = "El correo es obligatorio.")
        @Email(message = "El correo no tiene un formato valido.")
        @Size(max = 100, message = "El correo admite hasta 100 caracteres.")
        String correo,

        @NotBlank(message = "La contrasena es obligatoria.")
        @Pattern(regexp = Validaciones.CLAVE_FUERTE, message = Validaciones.MENSAJE_CLAVE)
        String password,

        @Size(max = 150, message = "La direccion admite hasta 150 caracteres.")
        String direccion) {
}
