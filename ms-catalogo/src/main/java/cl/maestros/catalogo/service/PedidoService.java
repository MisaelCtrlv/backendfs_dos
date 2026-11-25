package cl.maestros.catalogo.service;

import cl.maestros.catalogo.dto.CambioEstadoRequest;
import cl.maestros.catalogo.dto.PedidoItemRequest;
import cl.maestros.catalogo.dto.PedidoRequest;
import cl.maestros.catalogo.dto.PedidoResponse;
import cl.maestros.catalogo.dto.SaldoResponse;
import cl.maestros.catalogo.exception.ApiException;
import cl.maestros.catalogo.model.DetallePedido;
import cl.maestros.catalogo.model.EstadoPedido;
import cl.maestros.catalogo.model.Pedido;
import cl.maestros.catalogo.model.Producto;
import cl.maestros.catalogo.repository.PedidoRepository;
import cl.maestros.catalogo.repository.ProductoRepository;
import cl.maestros.catalogo.security.UsuarioAutenticado;
import jakarta.persistence.EntityManager;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PedidoService {

    public static final String PAGO_FIN_DE_MES = "Pago pendiente a fin de mes";
    public static final String PAGO_AL_RETIRAR = "Pago al retirar o recibir";

    private final PedidoRepository pedidos;
    private final ProductoRepository productos;
    private final EntityManager entityManager;

    public PedidoService(PedidoRepository pedidos, ProductoRepository productos, EntityManager entityManager) {
        this.pedidos = pedidos;
        this.productos = productos;
        this.entityManager = entityManager;
    }

    // Regla principal del caso: un pedido no puede superar el stock (409).
    // Todo ocurre en UNA transaccion: si algo falla, no se guarda el pedido ni se descuenta stock.
    @Transactional
    public PedidoResponse crear(PedidoRequest r, UsuarioAutenticado actor) {
        String tipoEntrega = r.tipoEntrega().trim().toLowerCase();
        String direccion = r.direccionDespacho() == null ? "" : r.direccionDespacho().trim();
        if (tipoEntrega.equals("despacho") && direccion.isEmpty()) {
            throw ApiException.invalido("La direccion de despacho es obligatoria.");
        }

        // Suma las cantidades si el mismo producto viene repetido
        Map<String, Integer> cantidades = new TreeMap<>();
        for (PedidoItemRequest item : r.items()) {
            cantidades.merge(item.codigo().trim().toUpperCase(), item.cantidad(), Integer::sum);
        }

        // Bloquea las filas de esos productos hasta terminar (SELECT ... FOR UPDATE)
        Map<String, Producto> porCodigo = productos.bloquearPorCodigos(cantidades.keySet()).stream()
                .collect(Collectors.toMap(Producto::getCodigo, Function.identity()));

        List<String> inexistentes = cantidades.keySet().stream().filter(c -> !porCodigo.containsKey(c)).toList();
        if (!inexistentes.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Hay productos que no existen.", inexistentes);
        }

        List<String> sinStock = new ArrayList<>();
        cantidades.forEach((codigo, cantidad) -> {
            Producto p = porCodigo.get(codigo);
            if (cantidad > p.getStock()) {
                sinStock.add(codigo + " " + p.getNombre() + ": pediste " + cantidad + ", hay " + p.getStock());
            }
        });
        if (!sinStock.isEmpty()) {
            throw new ApiException(HttpStatus.CONFLICT, "No hay stock suficiente para completar el pedido.", sinStock);
        }

        Pedido pedido = new Pedido();
        int id = pedidos.siguienteId().intValue();
        pedido.setId(id);
        pedido.setNumero(String.format("FM-%05d", id));
        pedido.setFecha(OffsetDateTime.now());
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setDireccionDespacho(tipoEntrega.equals("despacho") ? direccion : null);
        pedido.setEstado(EstadoPedido.PENDIENTE.getTexto());
        pedido.setCondicionPago(actor.esContratista() ? PAGO_FIN_DE_MES : PAGO_AL_RETIRAR);
        pedido.setIdUsuario(actor.id());

        int total = 0;
        for (Map.Entry<String, Integer> linea : cantidades.entrySet()) {
            Producto p = porCodigo.get(linea.getKey());
            int cantidad = linea.getValue();
            p.setStock(p.getStock() - cantidad); // se guarda al confirmar la transaccion
            DetallePedido detalle = new DetallePedido(p, cantidad, p.precioFinal());
            pedido.agregarDetalle(detalle);
            total += detalle.subtotal();
        }
        pedido.setTotal(total);

        entityManager.persist(pedido); // guarda el pedido y sus detalles (cascade)
        return PedidoResponse.desde(pedido);
    }

    // Admin y vendedor ven todos; cliente y contratista, solo los suyos
    @Transactional(readOnly = true)
    public List<PedidoResponse> listar(UsuarioAutenticado actor) {
        List<Pedido> lista = actor.esPersonal()
                ? pedidos.findAllByOrderByFechaDesc()
                : pedidos.findByIdUsuarioOrderByFechaDesc(actor.id());
        return lista.stream().map(PedidoResponse::desde).toList();
    }

    @Transactional(readOnly = true)
    public PedidoResponse obtener(String numero, UsuarioAutenticado actor) {
        return PedidoResponse.desde(buscarConAcceso(numero, actor));
    }

    // Solo avanza al siguiente estado; saltarse un paso o retroceder es un conflicto (409)
    @Transactional
    public PedidoResponse cambiarEstado(String numero, CambioEstadoRequest r, UsuarioAutenticado actor) {
        Pedido pedido = buscar(numero);
        EstadoPedido nuevo = EstadoPedido.desdeTexto(r.estado())
                .orElseThrow(() -> ApiException.invalido("Estado no valido: " + r.estado()));
        if (nuevo == EstadoPedido.ANULADO) {
            throw ApiException.invalido("Para anular un pedido usa PATCH /api/v1/pedidos/{numero}/anular.");
        }
        EstadoPedido actual = pedido.estadoActual();
        if (actual.siguiente().filter(nuevo::equals).isEmpty()) {
            throw ApiException.conflicto("No se puede pasar de '" + actual.getTexto() + "' a '" + nuevo.getTexto() + "'.");
        }
        pedido.setEstado(nuevo.getTexto());
        return PedidoResponse.desde(pedido);
    }

    // El dueno anula mientras este Pendiente; admin o vendedor tambien En preparacion. Devuelve el stock.
    @Transactional
    public PedidoResponse anular(String numero, UsuarioAutenticado actor) {
        Pedido pedido = buscarConAcceso(numero, actor);
        EstadoPedido actual = pedido.estadoActual();
        boolean permitido = actual == EstadoPedido.PENDIENTE
                || (actor.esPersonal() && actual == EstadoPedido.EN_PREPARACION);
        if (!permitido) {
            throw ApiException.conflicto("El pedido esta '" + actual.getTexto() + "' y ya no se puede anular.");
        }
        List<String> codigos = pedido.getDetalles().stream().map(d -> d.getProducto().getCodigo()).toList();
        Map<String, Producto> porCodigo = productos.bloquearPorCodigos(codigos).stream()
                .collect(Collectors.toMap(Producto::getCodigo, Function.identity()));
        for (DetallePedido d : pedido.getDetalles()) {
            Producto p = porCodigo.get(d.getProducto().getCodigo());
            p.setStock(p.getStock() + d.getCantidad());
        }
        pedido.setEstado(EstadoPedido.ANULADO.getTexto());
        return PedidoResponse.desde(pedido);
    }

    @Transactional(readOnly = true)
    public SaldoResponse saldo(UsuarioAutenticado actor) {
        if (!actor.esContratista()) {
            throw ApiException.prohibido("Solo los contratistas tienen cuenta corriente.");
        }
        long saldo = pedidos.sumarPendientes(actor.id(), PAGO_FIN_DE_MES, EstadoPedido.ANULADO.getTexto());
        return new SaldoResponse(actor.id(), saldo);
    }

    private Pedido buscar(String numero) {
        return pedidos.findByNumero(numero.trim().toUpperCase())
                .orElseThrow(() -> ApiException.noEncontrado("Pedido no encontrado."));
    }

    // Un cliente no puede ver ni anular pedidos de otra persona (403)
    private Pedido buscarConAcceso(String numero, UsuarioAutenticado actor) {
        Pedido pedido = buscar(numero);
        if (!actor.esPersonal() && !pedido.getIdUsuario().equals(actor.id())) {
            throw ApiException.prohibido("No puedes acceder a pedidos de otra persona.");
        }
        return pedido;
    }
}
