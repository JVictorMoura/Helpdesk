package helpdesk.dao;

import helpdesk.model.Chamado;
import helpdesk.model.Chamado.Prioridade;
import helpdesk.model.Chamado.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChamadoDAO {

    // ── CREATE ────────────────────────────────────────────
    public void inserir(Chamado ch) throws SQLException {
        // Regra de negócio: não abrir chamado para usuário inativo
        validarUsuarioAtivo(ch.getIdUsuario());

        String sql = "INSERT INTO chamado (titulo, descricao, prioridade, status, "
                   + "id_usuario, id_categoria, id_equipamento) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, ch.getTitulo());
            ps.setString(2, ch.getDescricao());
            ps.setString(3, ch.getPrioridade().name());
            ps.setString(4, ch.getStatus().name());
            ps.setInt   (5, ch.getIdUsuario());
            ps.setInt   (6, ch.getIdCategoria());
            if (ch.getIdEquipamento() != null) ps.setInt(7, ch.getIdEquipamento());
            else                               ps.setNull(7, Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) ch.setIdChamado(rs.getInt(1));
            }
        }
    }

    // ── READ (todos via VIEW) ─────────────────────────────
    public List<Chamado> listarTodos() throws SQLException {
        String sql = "SELECT * FROM vw_chamados ORDER BY id_chamado DESC";
        return executarConsulta(sql);
    }

    // ── READ (filtro por status) ───────────────────────────
    public List<Chamado> listarPorStatus(Status status) throws SQLException {
        String sql = "SELECT * FROM vw_chamados WHERE status = ? ORDER BY id_chamado DESC";
        List<Chamado> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapearView(rs));
            }
        }
        return lista;
    }

    // ── READ (por ID) ──────────────────────────────────────
    public Chamado buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM vw_chamados WHERE id_chamado = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearView(rs) : null;
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────
    public void atualizar(Chamado ch) throws SQLException {
        // Regra de negócio: chamado FECHADO não pode ser reaberto via update simples
        Chamado atual = buscarPorId(ch.getIdChamado());
        if (atual != null && atual.getStatus() == Status.FECHADO
                          && ch.getStatus()    != Status.FECHADO) {
            throw new SQLException("Chamado fechado não pode ser reaberto. Abra um novo chamado.");
        }

        String sql = "UPDATE chamado SET titulo=?, descricao=?, prioridade=?, status=?, "
                   + "id_categoria=?, id_equipamento=?, dt_fechamento=? "
                   + "WHERE id_chamado=?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, ch.getTitulo());
            ps.setString(2, ch.getDescricao());
            ps.setString(3, ch.getPrioridade().name());
            ps.setString(4, ch.getStatus().name());
            ps.setInt   (5, ch.getIdCategoria());
            if (ch.getIdEquipamento() != null) ps.setInt(7, ch.getIdEquipamento());
            else                               ps.setNull(6, Types.INTEGER);
            // Preenche dt_fechamento automaticamente ao fechar/resolver
            if (ch.getStatus() == Status.RESOLVIDO || ch.getStatus() == Status.FECHADO)
                 ps.setString(7, new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                     .format(new java.util.Date()));
            else ps.setNull(7, Types.VARCHAR);
            ps.setInt(8, ch.getIdChamado());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────
    public void excluir(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM chamado WHERE id_chamado=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── ATRIBUIR / REMOVER TÉCNICO (N:N) ──────────────────
    public void atribuirTecnico(int idChamado, int idTecnico, String obs) throws SQLException {
        String sql = "INSERT OR IGNORE INTO chamado_tecnico (id_chamado, id_tecnico, observacao) VALUES (?,?,?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt   (1, idChamado);
            ps.setInt   (2, idTecnico);
            ps.setString(3, obs);
            ps.executeUpdate();
        }
        // Avança status para EM_ATENDIMENTO se ainda ABERTO
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE chamado SET status='EM_ATENDIMENTO' WHERE id_chamado=? AND status='ABERTO'")) {
            ps.setInt(1, idChamado);
            ps.executeUpdate();
        }
    }

    public void removerTecnico(int idChamado, int idTecnico) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "DELETE FROM chamado_tecnico WHERE id_chamado=? AND id_tecnico=?")) {
            ps.setInt(1, idChamado);
            ps.setInt(2, idTecnico);
            ps.executeUpdate();
        }
    }

    public List<String> listarTecnicosDoChamado(int idChamado) throws SQLException {
        String sql = "SELECT tecnico, dt_atribuicao FROM vw_chamado_tecnicos WHERE id_chamado=?";
        List<String> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idChamado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next())
                    lista.add(rs.getString("tecnico") + "  (desde " + rs.getString("dt_atribuicao") + ")");
            }
        }
        return lista;
    }

    // ── Helpers ───────────────────────────────────────────
    private void validarUsuarioAtivo(int idUsuario) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT ativo FROM usuario WHERE id_usuario=?")) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt("ativo") == 0)
                    throw new SQLException("Usuário inativo ou inexistente. Não é permitido abrir chamado.");
            }
        }
    }

    private List<Chamado> executarConsulta(String sql) throws SQLException {
        List<Chamado> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearView(rs));
        }
        return lista;
    }

    private Chamado mapearView(ResultSet rs) throws SQLException {
        Chamado ch = new Chamado();
        ch.setIdChamado      (rs.getInt    ("id_chamado"));
        ch.setTitulo         (rs.getString ("titulo"));
        ch.setPrioridade     (Prioridade.valueOf(rs.getString("prioridade")));
        ch.setStatus         (Status.valueOf   (rs.getString("status")));
        ch.setDtAbertura     (rs.getString ("dt_abertura"));
        ch.setDtFechamento   (rs.getString ("dt_fechamento"));
        ch.setNomeUsuario    (rs.getString ("usuario"));
        ch.setNomeCategoria  (rs.getString ("categoria"));
        ch.setDescEquipamento(rs.getString ("equipamento"));
        return ch;
    }
}
