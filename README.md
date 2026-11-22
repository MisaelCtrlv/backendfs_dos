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

## Cómo ejecutar

Requisitos en la EC2: Java 21, Maven y acceso de red a la RDS.

```bash
cp .env.example .env      # completar DB_URL, DB_PASSWORD y JWT_SECRET
./ejecutar.sh ms-usuarios # puerto 8081
```

Swagger de ms-usuarios: `http://IP_PUBLICA_EC2:8081/swagger-ui.html`

Al arrancar, ms-usuarios crea los usuarios de prueba si no existen:

| Rol | Correo | Contraseña |
| --- | --- | --- |
| admin | admin@maestros.cl | `Admin123!` |
| vendedor | vendedor@maestros.cl | `Vendedor123!` |
| contratista | contratista@maestros.cl | `Contratista123!` |
| cliente | cliente@maestros.cl | `Cliente123!` |

## Endpoints de ms-usuarios

| Método | Ruta | Acceso | Respuestas |
| --- | --- | --- | --- |
| POST | `/api/v1/auth/registro` | Público (el servidor asigna el rol `cliente`) | 201, 400, 409 |
| POST | `/api/v1/auth/login` | Público | 200 con `accessToken`, 400, 401 |
| GET | `/api/v1/auth/me` | Con token | 200, 401 |
| GET | `/api/v1/usuarios` | admin | 200, 401, 403 |
| GET | `/api/v1/usuarios/{id}` | admin o el mismo usuario | 200, 401, 403, 404 |
| POST | `/api/v1/usuarios` | admin | 201, 400, 403, 409 |
| PUT | `/api/v1/usuarios/{id}` | admin o el mismo usuario (rol y estado: solo admin) | 200, 400, 403, 404 |
| DELETE | `/api/v1/usuarios/{id}` | admin (desactiva la cuenta) | 204, 403, 404, 409 |
| GET | `/api/v1/roles` | admin | 200, 401, 403 |

Los errores responden siempre `{ "mensaje": "...", "detalles": [...] }`.

## Seguridad

- Contraseñas guardadas con hash **BCrypt**; nunca se devuelven en las respuestas.
- **JWT** firmado con HS256 (`JWT_SECRET`), con `sub`, `id`, `rol`, `iss`, `aud` y vencimiento de 2 horas.
- Sin token, con token alterado o vencido: **401**. Con rol insuficiente o sobre datos ajenos: **403**.
- El rol de un registro público lo asigna el servidor; solo un administrador cambia roles.
- CORS limitado a los orígenes de `CORS_ORIGINS`. Credenciales solo en `.env`, fuera de Git.

## Estado del trabajo

| Operación | Diseñado | Implementado | Probado |
| --- | --- | --- | --- |
| Tabla de usuarios | Sí | Sí | No |
| Inicio de sesión (JWT) | Sí | Sí | No |
| Registro de usuario | Sí | Sí | No |
| Modificar usuario | Sí | Sí | No |
| Listar usuarios | Sí | Sí | No |
| Eliminar o desactivar usuario | Sí | Sí | No |
| CRUD de productos | Sí | No | No |
| Pedidos con control de stock (409) | Sí | No | No |

> Cambiar "Probado" a "Sí" cuando cada operación se pruebe en Swagger contra la base RDS.
