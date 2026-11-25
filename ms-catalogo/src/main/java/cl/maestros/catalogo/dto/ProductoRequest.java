package cl.maestros.catalogo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// Crear o modificar un producto. Al modificar, el codigo de la URL manda y este se ignora.
public record ProductoRequest(
        @Pattern(regexp = "^[A-Za-z]{2}\\d{3}$", message = "El codigo debe tener 2 letras y 3 numeros (ej. MC001).")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio.")
        @Size(max = 100, message = "El nombre admite hasta 100 caracteres.")
        String nombre,

        @NotBlank(message = "La marca es obligatoria.")
        @Size(max = 100, message = "La marca admite hasta 100 caracteres.")
        String marca,

        @NotBlank(message = "La unidad es obligatoria.")
        @Size(max = 50, message = "La unidad admite hasta 50 caracteres.")
        String unidad,

        @NotNull(message = "El precio de venta es obligatorio.")
        @Positive(message = "El precio de venta debe ser mayor que 0.")
        Integer precioVenta,

        @Positive(message = "El precio de oferta debe ser mayor que 0.")
        Integer precioOferta,

        @NotNull(message = "El stock es obligatorio.")
        @PositiveOrZero(message = "El stock no puede ser negativo.")
        Integer stock,

        @NotNull(message = "El stock minimo es obligatorio.")
        @PositiveOrZero(message = "El stock minimo no puede ser negativo.")
        Integer stockMinimo,

        Boolean destacado,

        @NotNull(message = "La categoria es obligatoria.")
        Short categoriaId) {
}
