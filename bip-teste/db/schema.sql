-- ============================================================
-- Schema principal do sistema de Benefícios
-- ============================================================

CREATE TABLE IF NOT EXISTS beneficio (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nome      VARCHAR(100)    NOT NULL,
    descricao VARCHAR(255),
    valor     DECIMAL(15, 2)  NOT NULL CHECK (valor >= 0),
    ativo     BOOLEAN         DEFAULT TRUE,
    versao    BIGINT          NOT NULL DEFAULT 0
);
