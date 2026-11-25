package cl.maestros.catalogo.repository;

import cl.maestros.catalogo.model.DetallePedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Integer> {

    // Un producto que aparece en algun pedido no se puede borrar
    boolean existsByProductoCodigo(String codigo);
}
