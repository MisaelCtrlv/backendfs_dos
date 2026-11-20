-- =====================================================================
-- Ferretería Los Maestros · Esquema de la base de datos (PostgreSQL)
-- Basado en el MER del equipo (SQL Developer Data Modeler), con los
-- ajustes del plan: password_hash, activo, estado legible, fecha, etc.
-- Ejecutar con el usuario maestro (postgres) en la base "ferreteria".
-- =====================================================================

-- ---------- ms-usuarios ----------
CREATE TABLE IF NOT EXISTS rol (
    id_rol  SMALLINT    PRIMARY KEY,
    nombre  VARCHAR(30) NOT NULL UNIQUE           -- admin, vendedor, contratista, cliente
);

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario     SERIAL       PRIMARY KEY,
    nombre         VARCHAR(50)  NOT NULL,
    apellidos      VARCHAR(100) NOT NULL,
    correo         VARCHAR(100) NOT NULL UNIQUE,   -- no se repiten cuentas
    password_hash  VARCHAR(100) NOT NULL,          -- hash BCrypt (60 caracteres), nunca texto plano
    direccion      VARCHAR(150),
    activo         BOOLEAN      NOT NULL DEFAULT TRUE, -- "eliminar" = desactivar
    id_rol         SMALLINT     NOT NULL REFERENCES rol (id_rol),
    creado_en      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- ---------- ms-catalogo ----------
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria      SMALLINT    PRIMARY KEY,
    nombre_categoria  VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS producto (
    codigo         VARCHAR(7)   PRIMARY KEY,       -- ej. MC001 (id_producto del MER)
    nombre         VARCHAR(100) NOT NULL,
    marca          VARCHAR(100) NOT NULL,
    unidad         VARCHAR(50)  NOT NULL,
    precio_venta   INTEGER      NOT NULL CHECK (precio_venta > 0),
    precio_oferta  INTEGER      CHECK (precio_oferta > 0),   -- NULL = sin oferta
    stock          INTEGER      NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo   INTEGER      NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    destacado      BOOLEAN      NOT NULL DEFAULT FALSE,
    id_categoria   SMALLINT     NOT NULL REFERENCES categoria (id_categoria),
    CONSTRAINT oferta_menor_que_precio CHECK (precio_oferta IS NULL OR precio_oferta < precio_venta)
);
CREATE INDEX IF NOT EXISTS idx_producto_categoria ON producto (id_categoria);

CREATE TABLE IF NOT EXISTS pedido (
    id_pedido           SERIAL       PRIMARY KEY,
    numero              VARCHAR(10)  NOT NULL UNIQUE,      -- FM-00001
    fecha               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    tipo_entrega        VARCHAR(20)  NOT NULL CHECK (tipo_entrega IN ('retiro', 'despacho')),
    direccion_despacho  VARCHAR(150),                      -- NULL si es retiro en tienda
    estado              VARCHAR(30)  NOT NULL DEFAULT 'Pendiente'
                        CHECK (estado IN ('Pendiente', 'En preparación', 'Listo para retiro o despacho', 'Entregado', 'Anulado')),
    condicion_pago      VARCHAR(50)  NOT NULL,             -- la calcula el servidor
    total               INTEGER      NOT NULL CHECK (total >= 0),
    id_usuario          INTEGER      NOT NULL REFERENCES usuarios (id_usuario),
    CONSTRAINT despacho_con_direccion CHECK (tipo_entrega = 'retiro' OR direccion_despacho IS NOT NULL)
);
CREATE INDEX IF NOT EXISTS idx_pedido_usuario ON pedido (id_usuario);

CREATE TABLE IF NOT EXISTS detalle_pedido (
    id_detalle       SERIAL     PRIMARY KEY,
    id_pedido        INTEGER    NOT NULL REFERENCES pedido (id_pedido) ON DELETE CASCADE,
    codigo_producto  VARCHAR(7) NOT NULL REFERENCES producto (codigo),  -- impide borrar un producto con pedidos
    cantidad         INTEGER    NOT NULL CHECK (cantidad > 0),
    precio_unitario  INTEGER    NOT NULL CHECK (precio_unitario >= 0)   -- precio al momento de comprar
);
