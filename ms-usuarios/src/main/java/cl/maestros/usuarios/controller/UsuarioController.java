package cl.maestros.usuarios.controller;

import cl.maestros.usuarios.dto.UsuarioActualizarRequest;
import cl.maestros.usuarios.dto.UsuarioCrearRequest;
import cl.maestros.usuarios.dto.UsuarioResponse;
import cl.maestros.usuarios.security.UsuarioAutenticado;
import cl.maestros.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "CRUD de usuarios con permisos por rol")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuarios (solo admin)")
    public List<UsuarioResponse> listar() {
        return usuarioService.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Ver un usuario (admin o el mismo usuario)")
    public UsuarioResponse obtener(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        return usuarioService.obtener(id, actor);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un usuario con rol (solo admin)")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCrearRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.id())).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modificar un usuario (admin o el mismo; rol y estado solo admin)")
    public UsuarioResponse actualizar(@PathVariable Integer id,
                                      @Valid @RequestBody UsuarioActualizarRequest request,
                                      @AuthenticationPrincipal UsuarioAutenticado actor) {
        return usuarioService.actualizar(id, request, actor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Desactivar un usuario (solo admin)")
    public ResponseEntity<Void> desactivar(@PathVariable Integer id, @AuthenticationPrincipal UsuarioAutenticado actor) {
        usuarioService.desactivar(id, actor);
        return ResponseEntity.noContent().build();
    }
}
