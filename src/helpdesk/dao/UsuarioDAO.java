package helpdesk.dao;

import helpdesk.model.Usuario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    // ── CREATE ────────────────────────────────────────────
    public void inserir(Usuario u) throws SQLException {
        String sql = "INSERT INTO usuario (nome, email, departamento, telefone, ativo) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getDepartamento());
            ps.setString(4, u.getTelefone());
            ps.setInt   (5, u.isAtivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) u.setIdUsuario(rs.getInt(1));
            }
        }
    }

    // ── READ (todos) ───────────────────────────────────────
    public List<Usuario> listarTodos() throws SQLException {
        String sql = "SELECT * FROM usuario ORDER BY nome";
        List<Usuario> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    // ── READ (por ID) ──────────────────────────────────────
    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM usuario WHERE id_usuario = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // ── UPDATE ────────────────────────────────────────────
    public void atualizar(Usuario u) throws SQLException {
        String sql = "UPDATE usuario SET nome=?, email=?, departamento=?, telefone=?, ativo=? "
                   + "WHERE id_usuario=?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, u.getNome());
            ps.setString(2, u.getEmail());
            ps.setString(3, u.getDepartamento());
            ps.setString(4, u.getTelefone());
            ps.setInt   (5, u.isAtivo() ? 1 : 0);
            ps.setInt   (6, u.getIdUsuario());
            ps.executeUpdate();
        }
    }

    // ── DELETE ────────────────────────────────────────────
    public void excluir(int id) throws SQLException {
        // Regra de negócio: não excluir usuário com chamados vinculados
        String check = "SELECT COUNT(*) FROM chamado WHERE id_usuario = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(check)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0)
                    throw new SQLException("Não é possível excluir: usuário possui chamados vinculados.");
            }
        }
        String sql = "DELETE FROM usuario WHERE id_usuario = ?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── Mapper ────────────────────────────────────────────
    private Usuario mapear(ResultSet rs) throws SQLException {
        return new Usuario(
            rs.getInt    ("id_usuario"),
            rs.getString ("nome"),
            rs.getString ("email"),
            rs.getString ("departamento"),
            rs.getString ("telefone"),
            rs.getInt    ("ativo") == 1
        );
    }
}
