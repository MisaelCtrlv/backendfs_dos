package cl.maestros.usuarios.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Tabla ROL del MER: admin, vendedor, contratista, cliente
@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @Column(name = "id_rol")
    private Short id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    public Short getId() { return id; }
    public String getNombre() { return nombre; }
}
