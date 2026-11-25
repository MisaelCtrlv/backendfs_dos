package cl.maestros.catalogo.controller;

import cl.maestros.catalogo.dto.ProductoRequest;
import cl.maestros.catalogo.dto.ProductoResponse;
import cl.maestros.catalogo.service.ProductoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catalogo: lectura publica; cambios segun rol")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping
    @Operation(summary = "Listar productos, con filtro opcional por categoria y texto (publico)")
    public List<ProductoResponse> listar(@RequestParam(required = false) Short categoria,
                                         @RequestParam(required = false) String texto) {
        return productoService.listar(categoria, texto);
    }

    @GetMapping("/{codigo}")
    @Operation(summary = "Ver un producto (publico)")
    public ProductoResponse obtener(@PathVariable String codigo) {
        return productoService.obtener(codigo);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un producto (solo admin)")
    public ResponseEntity<ProductoResponse> crear(@Valid @RequestBody ProductoRequest request) {
        ProductoResponse creado = productoService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/productos/" + creado.codigo())).body(creado);
    }

    @PutMapping("/{codigo}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDEDOR')")
    @Operation(summary = "Modificar un producto, por ejemplo su stock (admin o vendedor)")
    public ProductoResponse actualizar(@PathVariable String codigo, @Valid @RequestBody ProductoRequest request) {
        return productoService.actualizar(codigo, request);
    }

    @DeleteMapping("/{codigo}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Eliminar un producto sin pedidos (solo admin)")
    public ResponseEntity<Void> eliminar(@PathVariable String codigo) {
        productoService.eliminar(codigo);
        return ResponseEntity.noContent().build();
    }
}
