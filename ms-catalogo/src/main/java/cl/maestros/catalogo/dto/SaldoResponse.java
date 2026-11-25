package cl.maestros.catalogo.dto;

// Saldo de cuenta corriente del contratista: suma de pedidos a fin de mes no anulados
public record SaldoResponse(Integer idUsuario, long saldo) {
}
