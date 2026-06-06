# Helpdesk de TI — CRUD Java + SQLite
Laboratório de Banco de Dados | UCB 2025

## Tema
Sistema de gerenciamento de chamados de suporte técnico (Helpdesk de TI).

## Estrutura do Projeto
```
helpdesk/
├── db/
│   └── schema.sql          ← DDL + dados de exemplo
├── docs/
│   ├── der.png             ← Diagrama Entidade-Relacionamento
│   ├── modelo_logico.png   ← Modelo Lógico Relacional
│   └── diagrama_classes.png← Diagrama de Classes
└── src/helpdesk/
    ├── model/              ← Entidades do domínio (POJO)
    │   ├── Usuario.java
    │   ├── Tecnico.java
    │   ├── Categoria.java
    │   └── Chamado.java
    ├── dao/                ← Acesso a dados (JDBC)
    │   ├── ConnectionFactory.java
    │   ├── UsuarioDAO.java
    │   ├── TecnicoDAO.java
    │   ├── CategoriaDAO.java
    │   └── ChamadoDAO.java
    ├── service/
    │   └── DatabaseInitializer.java
    └── ui/                 ← Interface Swing
        ├── MainFrame.java
        ├── ChamadoPanel.java
        ├── UsuarioPanel.java
        └── TecnicoPanel.java
```

## Pré-requisitos
- Java 11 ou superior (JDK)
- sqlite-jdbc-3.x.x.jar (https://github.com/xerial/sqlite-jdbc/releases)

## Como Compilar e Executar

### 1. Compilar
```bash
# Windows
javac -cp ".;sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java

# Linux / macOS
javac -cp ".:sqlite-jdbc.jar" -d out -sourcepath src src/helpdesk/ui/MainFrame.java
```

### 2. Copiar schema.sql para o classpath
```bash
cp db/schema.sql out/schema.sql
```

### 3. Executar
```bash
# Windows
java -cp ".;sqlite-jdbc.jar;out" helpdesk.ui.MainFrame

# Linux / macOS
java -cp ".:sqlite-jdbc.jar:out" helpdesk.ui.MainFrame
```

O arquivo `helpdesk.db` será criado automaticamente na pasta de execução
com todas as tabelas e dados de exemplo na primeira execução.

## Tabelas do Banco
| Tabela           | Descrição                                         |
|------------------|---------------------------------------------------|
| usuario          | Usuários que abrem chamados                       |
| tecnico          | Técnicos de TI                                    |
| categoria        | Tipos de problema (Hardware, Software, Rede…)    |
| equipamento      | Patrimônio de TI vinculado ao usuário             |
| chamado          | Chamado de suporte (entidade principal)           |
| chamado_tecnico  | Associativa N:N — chamado ↔ técnico               |

## Regras de Negócio
1. Não é permitido abrir chamado para **usuário inativo**.
2. **Chamado fechado** não pode ser reaberto (deve abrir novo chamado).
3. Ao atribuir um técnico, chamado com status ABERTO avança automaticamente para EM_ATENDIMENTO.
4. Usuário com chamados vinculados não pode ser excluído.

## Relacionamentos
- usuario  1:N chamado
- categoria 1:N chamado
- equipamento 1:N chamado (opcional)
- usuario 1:1 equipamento (responsável)
- chamado N:N tecnico  → tabela `chamado_tecnico`

## Views disponíveis
- `vw_chamados` — chamados com nomes legíveis de usuário, categoria e equipamento
- `vw_chamado_tecnicos` — técnicos atribuídos por chamado
