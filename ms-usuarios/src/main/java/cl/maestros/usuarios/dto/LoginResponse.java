package cl.maestros.usuarios.dto;

// Respuesta del login: el token, su tipo, cuantos segundos dura y el usuario (sin hash)
public record LoginResponse(String accessToken, String tokenType, long expiresIn, UsuarioResponse usuario) {
}
