package cl.maestros.catalogo.repository;

import cl.maestros.catalogo.model.Categoria;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Short> {

    List<Categoria> findAllByOrderByIdAsc();
}
