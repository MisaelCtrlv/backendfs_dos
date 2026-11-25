package cl.maestros.catalogo.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla CATEGORIA del MER
@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @Column(name = "id_categoria")
    private Short id;

    @Column(name = "nombre_categoria", nullable = false, unique = true, length = 50)
    private String nombre;

    public Short getId() { return id; }
    public String getNombre() { return nombre; }
}
