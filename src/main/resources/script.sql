CREATE TABLE IF NOT EXISTS tb_cliente (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(100) NOT NULL,
    cpf         VARCHAR(14)  NOT NULL UNIQUE,
    cidade      VARCHAR(60),
    endereco    VARCHAR(150),
    estado      VARCHAR(2),
    casa_numero INTEGER,
    tel         VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS tb_produto (
    id        BIGSERIAL PRIMARY KEY,
    codigo    VARCHAR(30)    NOT NULL UNIQUE,
    nome      VARCHAR(100)   NOT NULL,
    descricao VARCHAR(255),
    valor     NUMERIC(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_venda (
    id          BIGSERIAL PRIMARY KEY,
    codigo      VARCHAR(30)    NOT NULL UNIQUE,
    cliente_id  BIGINT         NOT NULL REFERENCES tb_cliente (id),
    valor_total NUMERIC(12, 2) NOT NULL,
    data_venda  TIMESTAMP      NOT NULL,
    status      VARCHAR(20)    NOT NULL
);

CREATE TABLE IF NOT EXISTS tb_produto_quantidade (
    id          BIGSERIAL PRIMARY KEY,
    venda_id    BIGINT         NOT NULL REFERENCES tb_venda (id) ON DELETE CASCADE,
    produto_id  BIGINT         NOT NULL REFERENCES tb_produto (id),
    quantidade  INTEGER        NOT NULL,
    valor_total NUMERIC(12, 2) NOT NULL
);
