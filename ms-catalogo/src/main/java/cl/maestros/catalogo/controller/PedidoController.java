package cl.maestros.catalogo.controller;

import cl.maestros.catalogo.dto.CambioEstadoRequest;
import cl.maestros.catalogo.dto.PedidoRequest;
import cl.maestros.catalogo.dto.PedidoResponse;
import cl.maestros.catalogo.dto.SaldoResponse;
import cl.maestros.catalogo.security.UsuarioAutenticado;
import cl.maestros.catalogo.service.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pedidos")
@Tag(name = "Pedidos", description = "Solicitudes con control de stock")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE', 'CONTRATISTA')")
    @Operation(summary = "Crear un pedido (cliente o contratista). 409 si no hay stock")
    public ResponseEntity<PedidoResponse> crear(@Valid @RequestBody PedidoRequest request,
                                                @AuthenticationPrincipal UsuarioAutenticado actor) {
        PedidoResponse creado = pedidoService.crear(request, actor);
        return ResponseEntity.created(URI.create("/api/v1/pedidos/" + creado.numero())).body(creado);
    }

    @GetMapping
    @Operation(summary = "Listar pedidos: los propios, o todos si es admin o vendedor")
    public List<PedidoResponse> listar(@AuthenticationPrincipal UsuarioAutenticado actor) {
        return pedidoService.listar(actor);
    }

    @GetMapping("/saldo")
    @PreAuthorize("hasRole('CONTRATISTA')")
    @Operation(summary = "Saldo de cuenta corriente (solo contratista)")
    public SaldoResponse saldo(@AuthenticationPrincipal UsuarioAutenticado actor) {
        return pedidoService.saldo(actor);
    }

    @GetMapping("/{numero}")
    @Operation(summary = "Ver un pedido (dueno, admin o vendedor)")
    public PedidoResponse obtener(@PathVariable String numero, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return pedidoService.obtener(numero, actor);
    }

    @PatchMapping("/{numero}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Avanzar el estado del pedido (admin o vendedor). 409 si la transicion no es valida")
    public PedidoResponse cambiarEstado(@PathVariable String numero, @Valid @RequestBody CambioEstadoRequest request,
                                        @AuthenticationPrincipal UsuarioAutenticado actor) {
        return pedidoService.cambiarEstado(numero, request, actor);
    }

    @PatchMapping("/{numero}/anular")
    @Operation(summary = "Anular un pedido y devolver su stock (dueno si esta Pendiente; admin o vendedor)")
    public PedidoResponse anular(@PathVariable String numero, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return pedidoService.anular(numero, actor);
    }
}
