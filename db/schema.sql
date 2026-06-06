-- ============================================================
--  HELPDESK DE TI — schema.sql
--  Laboratório de Banco de Dados — UCB 2025
-- ============================================================

-- Remover tabelas na ordem inversa de dependência
DROP TABLE IF EXISTS chamado_tecnico;
DROP TABLE IF EXISTS chamado;
DROP TABLE IF EXISTS tecnico;
DROP TABLE IF EXISTS equipamento;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS usuario;

-- ============================================================
--  1. USUÁRIO  (quem abre chamados)
-- ============================================================
CREATE TABLE usuario (
    id_usuario   INTEGER      PRIMARY KEY AUTOINCREMENT,
    nome         VARCHAR(100) NOT NULL,
    email        VARCHAR(150) NOT NULL UNIQUE,
    departamento VARCHAR(80)  NOT NULL,
    telefone     VARCHAR(20),
    ativo        INTEGER      NOT NULL DEFAULT 1
                              CHECK (ativo IN (0, 1))
);

-- ============================================================
--  2. CATEGORIA  (tipo do problema: Hardware, Software, Rede…)
-- ============================================================
CREATE TABLE categoria (
    id_categoria INTEGER     PRIMARY KEY AUTOINCREMENT,
    nome         VARCHAR(80) NOT NULL UNIQUE,
    descricao    TEXT
);

-- ============================================================
--  3. EQUIPAMENTO  (patrimônio de TI)
-- ============================================================
CREATE TABLE equipamento (
    id_equipamento INTEGER      PRIMARY KEY AUTOINCREMENT,
    patrimonio     VARCHAR(30)  NOT NULL UNIQUE,
    descricao      VARCHAR(150) NOT NULL,
    localizacao    VARCHAR(100),
    id_usuario     INTEGER      REFERENCES usuario(id_usuario)
                                ON DELETE SET NULL
);

-- ============================================================
--  4. TECNICO  (quem atende chamados)
-- ============================================================
CREATE TABLE tecnico (
    id_tecnico INTEGER      PRIMARY KEY AUTOINCREMENT,
    nome       VARCHAR(100) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    login      VARCHAR(50)  NOT NULL UNIQUE,
    ativo      INTEGER      NOT NULL DEFAULT 1
                            CHECK (ativo IN (0, 1))
);

-- ============================================================
--  5. CHAMADO  (entidade principal)
--     Status: ABERTO | EM_ATENDIMENTO | RESOLVIDO | FECHADO
-- ============================================================
CREATE TABLE chamado (
    id_chamado    INTEGER      PRIMARY KEY AUTOINCREMENT,
    titulo        VARCHAR(200) NOT NULL,
    descricao     TEXT         NOT NULL,
    prioridade    VARCHAR(10)  NOT NULL DEFAULT 'MEDIA'
                               CHECK (prioridade IN ('BAIXA','MEDIA','ALTA','CRITICA')),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ABERTO'
                               CHECK (status IN ('ABERTO','EM_ATENDIMENTO','RESOLVIDO','FECHADO')),
    dt_abertura   TEXT         NOT NULL DEFAULT (datetime('now','localtime')),
    dt_fechamento TEXT,
    id_usuario    INTEGER      NOT NULL REFERENCES usuario(id_usuario),
    id_categoria  INTEGER      NOT NULL REFERENCES categoria(id_categoria),
    id_equipamento INTEGER     REFERENCES equipamento(id_equipamento)
);

-- ============================================================
--  6. CHAMADO_TECNICO  (N:N — chamado pode ter vários técnicos)
-- ============================================================
CREATE TABLE chamado_tecnico (
    id_chamado INTEGER NOT NULL REFERENCES chamado(id_chamado)   ON DELETE CASCADE,
    id_tecnico INTEGER NOT NULL REFERENCES tecnico(id_tecnico)   ON DELETE CASCADE,
    dt_atribuicao TEXT NOT NULL DEFAULT (datetime('now','localtime')),
    observacao    TEXT,
    PRIMARY KEY (id_chamado, id_tecnico)
);

-- ============================================================
--  VIEWS
-- ============================================================

-- Vista resumo de chamados com nomes legíveis
CREATE VIEW vw_chamados AS
SELECT
    c.id_chamado,
    c.titulo,
    c.prioridade,
    c.status,
    c.dt_abertura,
    c.dt_fechamento,
    u.nome        AS usuario,
    u.departamento,
    cat.nome      AS categoria,
    e.descricao   AS equipamento
FROM chamado c
JOIN usuario   u   ON c.id_usuario    = u.id_usuario
JOIN categoria cat ON c.id_categoria  = cat.id_categoria
LEFT JOIN equipamento e ON c.id_equipamento = e.id_equipamento;

-- Vista de técnicos atribuídos por chamado
CREATE VIEW vw_chamado_tecnicos AS
SELECT
    ct.id_chamado,
    c.titulo,
    t.nome   AS tecnico,
    t.email  AS email_tecnico,
    ct.dt_atribuicao,
    ct.observacao
FROM chamado_tecnico ct
JOIN chamado c ON ct.id_chamado = c.id_chamado
JOIN tecnico t ON ct.id_tecnico  = t.id_tecnico;

-- ============================================================
--  DADOS DE EXEMPLO
-- ============================================================
INSERT INTO categoria (nome, descricao) VALUES
    ('Hardware',    'Problemas físicos em equipamentos'),
    ('Software',    'Erros em sistemas e aplicativos'),
    ('Rede',        'Conectividade, VPN, Wi-Fi'),
    ('Acesso',      'Senhas, permissões, contas AD'),
    ('Impressora',  'Impressoras e multifuncionais');

INSERT INTO usuario (nome, email, departamento, telefone) VALUES
    ('Ana Lima',       'ana.lima@empresa.com',    'RH',        '61-91111-0001'),
    ('Bruno Souza',    'bruno.souza@empresa.com', 'Financeiro','61-91111-0002'),
    ('Carla Mendes',   'carla.mendes@empresa.com','Jurídico',  '61-91111-0003');

INSERT INTO tecnico (nome, email, login) VALUES
    ('Diego TI',    'diego@ti.empresa.com',   'diego'),
    ('Fernanda TI', 'fernanda@ti.empresa.com','fernanda');

INSERT INTO equipamento (patrimonio, descricao, localizacao, id_usuario) VALUES
    ('TI-0042', 'Notebook Dell Latitude 5420', 'Sala RH',       1),
    ('TI-0099', 'Desktop HP EliteDesk',        'Financeiro 2',  2);

INSERT INTO chamado (titulo, descricao, prioridade, status, id_usuario, id_categoria, id_equipamento) VALUES
    ('Notebook não liga',   'Ao pressionar o botão liga/desliga nada acontece.', 'ALTA',  'ABERTO',          1, 1, 1),
    ('Sem acesso ao VPN',   'VPN retorna erro de autenticação desde ontem.',     'MEDIA', 'EM_ATENDIMENTO',  2, 3, NULL),
    ('Impressora offline',  'Impressora do jurídico aparece offline na rede.',   'BAIXA', 'ABERTO',          3, 5, NULL);

INSERT INTO chamado_tecnico (id_chamado, id_tecnico, observacao) VALUES
    (1, 1, 'Verificando fonte de alimentação'),
    (2, 1, 'Analisando logs do cliente VPN'),
    (2, 2, 'Suporte remoto agendado');
