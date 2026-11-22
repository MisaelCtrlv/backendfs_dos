package cl.maestros.usuarios.controller;

import cl.maestros.usuarios.dto.LoginRequest;
import cl.maestros.usuarios.dto.LoginResponse;
import cl.maestros.usuarios.dto.RegistroRequest;
import cl.maestros.usuarios.dto.UsuarioResponse;
import cl.maestros.usuarios.security.UsuarioAutenticado;
import cl.maestros.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticacion", description = "Registro, inicio de sesion y usuario actual")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    @Operation(summary = "Registrar una cuenta (siempre con rol cliente)")
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse creado = authService.registrar(request);
        return ResponseEntity.created(URI.create("/api/v1/usuarios/" + creado.id())).body(creado);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion y obtener un JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "Datos del usuario dueno del token")
    public UsuarioResponse yo(@AuthenticationPrincipal UsuarioAutenticado actor) {
        return authService.yo(actor);
    }
}
