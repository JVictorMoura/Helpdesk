# Helpdesk de TI

> Laboratório de Banco de Dados | UCB 2026

Sistema desktop de gerenciamento de chamados de suporte técnico, desenvolvido com Java Swing, JDBC e banco de dados SQLite. Implementa o padrão arquitetural DAO e demonstra os fundamentos de CRUD, integridade referencial e relacionamentos N:N em banco relacional.

---

## Apresentação rápida

> Execute os comandos abaixo a partir da pasta `helpdesk/`. Na primeira execução o banco é criado e populado automaticamente.

**1. Compilar**
```bash
# Windows
javac -cp ".;sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java

# Linux / macOS
javac -cp ".:sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java
```

**2. Copiar o schema** *(obrigatório na primeira vez)*
```bash
cp db/schema.sql out/schema.sql
```

**3. Executar**
```bash
# Windows
java -cp ".;sqlite-jdbc.jar;out" helpdesk.ui.MainFrame

# Linux / macOS
java -cp ".:sqlite-jdbc.jar:out" helpdesk.ui.MainFrame
```

###  Onde estão os scripts

| O que você procura | Onde fica |
|--------------------|-----------|
| DDL das tabelas, views e dados de exemplo | `db/schema.sql` |
| Diagramas (DER, modelo lógico, classes) | `docs/diagramas.html` |
| Código de acesso ao banco (DAOs) | `src/helpdesk/dao/` |
| Inicialização automática do banco | `src/helpdesk/service/DatabaseInitializer.java` |

---

## Sumário

- [Visão Geral](#visão-geral)
- [Pré-requisitos](#pré-requisitos)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Como Compilar e Executar](#como-compilar-e-executar)
- [Banco de Dados](#banco-de-dados)
- [Funcionalidades](#funcionalidades)
- [Regras de Negócio](#regras-de-negócio)
- [Arquitetura](#arquitetura)

---

## Visão Geral

O sistema permite cadastrar e gerenciar chamados de suporte de TI, associando-os a usuários, categorias, equipamentos e técnicos responsáveis. Toda a persistência é feita em um arquivo SQLite local (`helpdesk.db`), criado e populado automaticamente na primeira execução.

**Tecnologias utilizadas:**

| Camada | Tecnologia |
|--------|-----------|
| Interface gráfica | Java Swing |
| Acesso a dados | JDBC |
| Banco de dados | SQLite 3 |
| Driver JDBC | sqlite-jdbc |

---

## Pré-requisitos

- **JDK 11** ou superior
- **sqlite-jdbc** (arquivo `.jar`) — download em [github.com/xerial/sqlite-jdbc/releases](https://github.com/xerial/sqlite-jdbc/releases)

Coloque o arquivo `sqlite-jdbc.jar` na raiz da pasta `helpdesk/` antes de compilar.

---

## Estrutura do Projeto

```
helpdesk/
├── db/
│   └── schema.sql              ← DDL das tabelas, views e dados de exemplo
├── docs/
│   └── diagramas.html          ← DER, modelo lógico e diagrama de classes
└── src/helpdesk/
    ├── model/                  ← Entidades do domínio (POJOs)
    │   ├── Usuario.java
    │   ├── Tecnico.java
    │   ├── Categoria.java
    │   └── Chamado.java
    ├── dao/                    ← Camada de acesso a dados (padrão DAO + JDBC)
    │   ├── ConnectionFactory.java
    │   ├── UsuarioDAO.java
    │   ├── TecnicoDAO.java
    │   ├── CategoriaDAO.java
    │   └── ChamadoDAO.java
    ├── service/
    │   └── DatabaseInitializer.java  ← Inicialização automática do banco
    └── ui/                     ← Interface gráfica (Java Swing)
        ├── MainFrame.java
        ├── ChamadoPanel.java
        ├── UsuarioPanel.java
        └── TecnicoPanel.java
```

---

## Como Compilar e Executar

Execute todos os comandos a partir da pasta `helpdesk/`.

### 1. Compilar

**Windows:**
```bash
javac -cp ".;sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java
```

**Linux / macOS:**
```bash
javac -cp ".:sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java
```

### 2. Copiar o schema para o classpath

```bash
cp db/schema.sql out/schema.sql
```

> Este passo é obrigatório. O `DatabaseInitializer` localiza o `schema.sql` via classpath na primeira execução.

### 3. Executar

**Windows:**
```bash
java -cp ".;sqlite-jdbc.jar;out" helpdesk.ui.MainFrame
```

**Linux / macOS:**
```bash
java -cp ".:sqlite-jdbc.jar:out" helpdesk.ui.MainFrame
```

Na primeira execução, o arquivo `helpdesk.db` é criado automaticamente na pasta corrente com todas as tabelas e dados de exemplo já inseridos.

---

## Banco de Dados

### Tabelas

| Tabela | Descrição |
|--------|-----------|
| `usuario` | Usuários que abrem chamados |
| `tecnico` | Técnicos de TI responsáveis pelo atendimento |
| `categoria` | Tipos de problema: Hardware, Software, Rede, etc. |
| `equipamento` | Patrimônio de TI vinculado a um usuário |
| `chamado` | Entidade principal — chamado de suporte |
| `chamado_tecnico` | Tabela associativa N:N entre chamado e técnico |

### Views

| View | Descrição |
|------|-----------|
| `vw_chamados` | Chamados com nomes legíveis de usuário, categoria e equipamento |
| `vw_chamado_tecnicos` | Técnicos atribuídos por chamado, com data e observação |

### Relacionamentos

```
usuario     1 ──────── N  chamado
categoria   1 ──────── N  chamado
equipamento 1 ──────── N  chamado   (opcional)
usuario     1 ──────── 1  equipamento  (responsável)
chamado     N ──────── N  tecnico   → chamado_tecnico
```

### Status do chamado

Um chamado percorre os seguintes estados:

```
ABERTO  →  EM_ATENDIMENTO  →  RESOLVIDO  →  FECHADO
```

### Dados de exemplo

O `schema.sql` insere automaticamente:

- 5 categorias (Hardware, Software, Rede, Acesso, Impressora)
- 3 usuários (Ana Lima, Bruno Souza, Carla Mendes)
- 2 técnicos (Diego TI, Fernanda TI)
- 2 equipamentos vinculados a usuários
- 3 chamados com diferentes status e prioridades

---

## Funcionalidades

### Chamados
- Criar, listar, editar e excluir chamados
- Filtrar chamados por status
- Atribuir e remover técnicos responsáveis (N:N)
- Campos: título, descrição, prioridade, status, usuário, categoria

### Usuários
- CRUD completo de usuários
- Campos: nome, e-mail, departamento, telefone, ativo/inativo

### Técnicos
- CRUD completo de técnicos
- Campos: nome, e-mail, login, ativo/inativo

---

## Regras de Negócio

1. Não é permitido abrir chamado para **usuário inativo**.
2. **Chamado fechado** não pode ser reaberto — deve-se abrir um novo chamado.
3. Ao atribuir um técnico, chamado com status `ABERTO` avança automaticamente para `EM_ATENDIMENTO`.
4. **Usuário com chamados vinculados não pode ser excluído** — a integridade referencial é garantida pelo SQLite com `PRAGMA foreign_keys = ON`.

---

## Arquitetura

O projeto segue o padrão **DAO (Data Access Object)**, separando as responsabilidades em três camadas:

```
┌─────────────────────────────────┐
│     UI (Java Swing)             │  Apresentação e interação com o usuário
│  MainFrame / *Panel             │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│     DAO (JDBC)                  │  Acesso e persistência de dados
│  *DAO / ConnectionFactory       │
└────────────────┬────────────────┘
                 │
┌────────────────▼────────────────┐
│     Banco de Dados (SQLite)     │  Armazenamento local em arquivo .db
│  helpdesk.db                    │
└─────────────────────────────────┘
```

A `ConnectionFactory` fornece conexões JDBC e ativa as foreign keys (`PRAGMA foreign_keys = ON`) a cada conexão aberta, garantindo a integridade referencial do banco.

---

