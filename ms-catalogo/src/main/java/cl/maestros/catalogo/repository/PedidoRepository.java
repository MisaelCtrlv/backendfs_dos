package cl.maestros.catalogo.repository;

import cl.maestros.catalogo.model.Pedido;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {

    Optional<Pedido> findByNumero(String numero);

    List<Pedido> findAllByOrderByFechaDesc();

    List<Pedido> findByIdUsuarioOrderByFechaDesc(Integer idUsuario);

    // Siguiente valor de la secuencia que creo SERIAL en id_pedido
    @Query(value = "SELECT nextval('pedido_id_pedido_seq')", nativeQuery = true)
    Long siguienteId();

    // Saldo de cuenta corriente: pedidos a fin de mes que no estan anulados
    @Query("select coalesce(sum(p.total), 0) from Pedido p "
            + "where p.idUsuario = :idUsuario and p.condicionPago = :condicion and p.estado <> :anulado")
    Long sumarPendientes(@Param("idUsuario") Integer idUsuario,
                        @Param("condicion") String condicion,
                        @Param("anulado") String anulado);
}
