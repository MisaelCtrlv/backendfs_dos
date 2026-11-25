package cl.maestros.catalogo.repository;

import cl.maestros.catalogo.model.Producto;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findAllByOrderByCodigoAsc();

    // SELECT ... FOR UPDATE: bloquea las filas hasta terminar la transaccion,
    // asi dos pedidos simultaneos no pueden vender la misma ultima unidad.
    // Se ordena por codigo para que dos transacciones bloqueen en el mismo orden.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Producto p where p.codigo in :codigos order by p.codigo")
    List<Producto> bloquearPorCodigos(@Param("codigos") Collection<String> codigos);
}
