# Backend – Ferretería Los Maestros

API REST en **Spring Boot 3 (Java 21)** con **Amazon RDS for PostgreSQL**, JWT y Swagger.
Proyecto semestral DSY1104 · Desarrollo Fullstack II · Sección DSY1104-005D.

**Integrantes:** Misael Concha, Bastián Araneda
**Frontend:** https://github.com/Bastian0711/Ferreteria-los-maestros

## Arquitectura

| Microservicio | Puerto | Tablas | Responsabilidad |
| --- | --- | --- | --- |
| `ms-usuarios` | 8081 | rol, usuarios | Registro, login con JWT, usuarios y roles |
| `ms-catalogo` | 8082 | categoria, producto, pedido, detalle_pedido | Catálogo y pedidos con control de stock |

Ambos corren en una instancia EC2 (Ubuntu 24.04) y se conectan con TLS a una base RDS PostgreSQL
privada (`ferreteria-db`, sin acceso público; el security group solo acepta el puerto 5432 desde la EC2).

## Base de datos

Los scripts están en `sql/` y se ejecutan con `psql` desde la EC2:

1. `01_esquema.sql`: tablas del MER con claves primarias, foráneas, `UNIQUE` y `CHECK`.
2. `02_datos.sql`: 4 roles, 10 categorías y 85 productos (los mismos datos simulados del frontend).
3. `03_usuario_app.sql`: usuario `app_ferreteria` con permisos solo sobre los datos.

## Estado del trabajo

| Operación | Diseñado | Implementado | Probado |
| --- | --- | --- | --- |
| Tabla de usuarios | Sí | Sí (script SQL) | No |
| Inicio de sesión (JWT) | Sí | No | No |
| Registro de usuario | Sí | No | No |
| Modificar usuario | Sí | No | No |
| Listar usuarios | Sí | No | No |
| Eliminar o desactivar usuario | Sí | No | No |
| CRUD de productos | Sí | No | No |
| Pedidos con control de stock (409) | Sí | No | No |
