package cl.maestros.usuarios.repository;

import cl.maestros.usuarios.model.Rol;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Short> {

    Optional<Rol> findByNombre(String nombre);

    List<Rol> findAllByOrderByIdAsc();
}
