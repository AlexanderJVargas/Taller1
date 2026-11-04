-- =====================================================
-- Cafetería UTN - Modelo relacional v2 (PostgreSQL / pgAdmin)
-- Identificadores VARCHAR (sin SERIAL), sin N:M, sin ciclos
-- =====================================================

DROP TABLE IF EXISTS auditoria_stock CASCADE;
DROP TABLE IF EXISTS detalle_venta_bebida CASCADE;
DROP TABLE IF EXISTS detalle_venta_plato CASCADE;
DROP TABLE IF EXISTS venta CASCADE;
DROP TABLE IF EXISTS detalle_abastecimiento_bebida CASCADE;
DROP TABLE IF EXISTS abastecimiento_bebida CASCADE;
DROP TABLE IF EXISTS detalle_abastecimiento_ingrediente CASCADE;
DROP TABLE IF EXISTS abastecimiento_ingrediente CASCADE;
DROP TABLE IF EXISTS detalle_receta CASCADE;
DROP TABLE IF EXISTS bebida CASCADE;
DROP TABLE IF EXISTS plato CASCADE;
DROP TABLE IF EXISTS ingrediente CASCADE;

-- ---------- Catálogos ----------

CREATE TABLE ingrediente (
    id_ingrediente  VARCHAR(10)   NOT NULL,
    nombre          VARCHAR(60)   NOT NULL,
    unidad_medida   VARCHAR(2)    NOT NULL,          -- 'kg' o 'l'
    costo_unitario  NUMERIC(10,4) NOT NULL,          -- costo por kg o litro
    stock_actual    NUMERIC(10,3) NOT NULL DEFAULT 0,
    stock_minimo    NUMERIC(10,3) NOT NULL DEFAULT 0,
    CONSTRAINT pk_ingrediente PRIMARY KEY (id_ingrediente),
    CONSTRAINT uq_ingrediente_nombre UNIQUE (nombre),
    CONSTRAINT ck_ingrediente_unidad CHECK (unidad_medida IN ('kg', 'l')),
    CONSTRAINT ck_ingrediente_costo CHECK (costo_unitario >= 0),
    CONSTRAINT ck_ingrediente_stock CHECK (stock_actual >= 0),
    CONSTRAINT ck_ingrediente_minimo CHECK (stock_minimo >= 0)
);

CREATE TABLE plato (
    id_plato  VARCHAR(10)  NOT NULL,
    nombre    VARCHAR(60)  NOT NULL,
    CONSTRAINT pk_plato PRIMARY KEY (id_plato),
    CONSTRAINT uq_plato_nombre UNIQUE (nombre)
    -- Costo (suma de ingredientes) y precio de venta (2 x costo) se calculan.
);

CREATE TABLE bebida (
    id_bebida    VARCHAR(10)   NOT NULL,
    nombre       VARCHAR(60)   NOT NULL,
    precio_fijo  NUMERIC(10,2) NOT NULL,             -- sin IVA
    stock        INTEGER       NOT NULL DEFAULT 0,   -- en unidades
    CONSTRAINT pk_bebida PRIMARY KEY (id_bebida),
    CONSTRAINT uq_bebida_nombre UNIQUE (nombre),
    CONSTRAINT ck_bebida_precio CHECK (precio_fijo >= 0),
    CONSTRAINT ck_bebida_stock CHECK (stock >= 0)
);

-- ---------- Receta (detalle plato - ingrediente) ----------

CREATE TABLE detalle_receta (
    id_plato        VARCHAR(10)   NOT NULL,
    id_ingrediente  VARCHAR(10)   NOT NULL,
    cantidad        NUMERIC(10,3) NOT NULL,          -- kg o l por plato
    CONSTRAINT pk_detalle_receta PRIMARY KEY (id_plato, id_ingrediente),
    CONSTRAINT fk_receta_plato FOREIGN KEY (id_plato)
        REFERENCES plato (id_plato) ON DELETE CASCADE,
    -- RESTRICT: no se elimina un ingrediente que forma parte de una receta
    CONSTRAINT fk_receta_ingrediente FOREIGN KEY (id_ingrediente)
        REFERENCES ingrediente (id_ingrediente) ON DELETE RESTRICT,
    CONSTRAINT ck_receta_cantidad CHECK (cantidad > 0)
);

-- ---------- Abastecimiento de ingredientes ----------

CREATE TABLE abastecimiento_ingrediente (
    id_abast_ingrediente  VARCHAR(10) NOT NULL,
    fecha                 DATE        NOT NULL,
    CONSTRAINT pk_abast_ingrediente PRIMARY KEY (id_abast_ingrediente)
);

CREATE TABLE detalle_abastecimiento_ingrediente (
    id_abast_ingrediente  VARCHAR(10)   NOT NULL,
    id_ingrediente        VARCHAR(10)   NOT NULL,
    cantidad              NUMERIC(10,3) NOT NULL,
    costo_unitario        NUMERIC(10,4) NOT NULL,    -- costo pagado en esa compra
    CONSTRAINT pk_det_abast_ing PRIMARY KEY (id_abast_ingrediente, id_ingrediente),
    CONSTRAINT fk_dai_abastecimiento FOREIGN KEY (id_abast_ingrediente)
        REFERENCES abastecimiento_ingrediente (id_abast_ingrediente) ON DELETE CASCADE,
    CONSTRAINT fk_dai_ingrediente FOREIGN KEY (id_ingrediente)
        REFERENCES ingrediente (id_ingrediente) ON DELETE RESTRICT,
    CONSTRAINT ck_dai_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_dai_costo CHECK (costo_unitario >= 0)
);

-- ---------- Abastecimiento de bebidas ----------

CREATE TABLE abastecimiento_bebida (
    id_abast_bebida  VARCHAR(10) NOT NULL,
    fecha            DATE        NOT NULL,
    CONSTRAINT pk_abast_bebida PRIMARY KEY (id_abast_bebida)
);

CREATE TABLE detalle_abastecimiento_bebida (
    id_abast_bebida  VARCHAR(10) NOT NULL,
    id_bebida        VARCHAR(10) NOT NULL,
    cantidad         INTEGER     NOT NULL,
    CONSTRAINT pk_det_abast_beb PRIMARY KEY (id_abast_bebida, id_bebida),
    CONSTRAINT fk_dab_abastecimiento FOREIGN KEY (id_abast_bebida)
        REFERENCES abastecimiento_bebida (id_abast_bebida) ON DELETE CASCADE,
    CONSTRAINT fk_dab_bebida FOREIGN KEY (id_bebida)
        REFERENCES bebida (id_bebida) ON DELETE RESTRICT,
    CONSTRAINT ck_dab_cantidad CHECK (cantidad > 0)
);

-- ---------- Ventas ----------

CREATE TABLE venta (
    id_venta  VARCHAR(10)   NOT NULL,
    fecha     DATE          NOT NULL,
    subtotal  NUMERIC(10,2) NOT NULL DEFAULT 0,
    iva       NUMERIC(10,2) NOT NULL DEFAULT 0,      -- 15 % solo sobre bebidas
    total     NUMERIC(10,2) NOT NULL DEFAULT 0,
    CONSTRAINT pk_venta PRIMARY KEY (id_venta),
    CONSTRAINT ck_venta_valores CHECK (subtotal >= 0 AND iva >= 0 AND total >= 0)
);

CREATE TABLE detalle_venta_plato (
    id_venta         VARCHAR(10)   NOT NULL,
    id_plato         VARCHAR(10)   NOT NULL,
    cantidad         INTEGER       NOT NULL,
    precio_unitario  NUMERIC(10,2) NOT NULL,         -- precio al momento de la venta
    CONSTRAINT pk_det_venta_plato PRIMARY KEY (id_venta, id_plato),
    CONSTRAINT fk_dvp_venta FOREIGN KEY (id_venta)
        REFERENCES venta (id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_dvp_plato FOREIGN KEY (id_plato)
        REFERENCES plato (id_plato) ON DELETE RESTRICT,
    CONSTRAINT ck_dvp_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_dvp_precio CHECK (precio_unitario >= 0)
);

CREATE TABLE detalle_venta_bebida (
    id_venta         VARCHAR(10)   NOT NULL,
    id_bebida        VARCHAR(10)   NOT NULL,
    cantidad         INTEGER       NOT NULL,
    precio_unitario  NUMERIC(10,2) NOT NULL,         -- sin IVA, al momento de la venta
    CONSTRAINT pk_det_venta_bebida PRIMARY KEY (id_venta, id_bebida),
    CONSTRAINT fk_dvb_venta FOREIGN KEY (id_venta)
        REFERENCES venta (id_venta) ON DELETE CASCADE,
    CONSTRAINT fk_dvb_bebida FOREIGN KEY (id_bebida)
        REFERENCES bebida (id_bebida) ON DELETE RESTRICT,
    CONSTRAINT ck_dvb_cantidad CHECK (cantidad > 0),
    CONSTRAINT ck_dvb_precio CHECK (precio_unitario >= 0)
);

-- ---------- Auditoría del stock de ingredientes ----------

CREATE TABLE auditoria_stock (
    id_auditoria     VARCHAR(10)   NOT NULL,
    id_ingrediente   VARCHAR(10)   NOT NULL,
    fecha            TIMESTAMP     NOT NULL,
    tipo_movimiento  VARCHAR(15)   NOT NULL,         -- ABASTECIMIENTO o VENTA
    id_referencia    VARCHAR(10)   NOT NULL,         -- id de la venta o del abastecimiento
    cantidad         NUMERIC(10,3) NOT NULL,
    stock_anterior   NUMERIC(10,3) NOT NULL,
    stock_nuevo      NUMERIC(10,3) NOT NULL,
    CONSTRAINT pk_auditoria_stock PRIMARY KEY (id_auditoria),
    CONSTRAINT fk_auditoria_ingrediente FOREIGN KEY (id_ingrediente)
        REFERENCES ingrediente (id_ingrediente) ON DELETE RESTRICT,
    CONSTRAINT ck_auditoria_tipo CHECK (tipo_movimiento IN ('ABASTECIMIENTO', 'VENTA'))
);
