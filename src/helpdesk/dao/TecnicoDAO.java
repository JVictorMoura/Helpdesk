package helpdesk.dao;

import helpdesk.model.Tecnico;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TecnicoDAO {

    public void inserir(Tecnico t) throws SQLException {
        String sql = "INSERT INTO tecnico (nome, email, login, ativo) VALUES (?,?,?,?)";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getEmail());
            ps.setString(3, t.getLogin());
            ps.setInt   (4, t.isAtivo() ? 1 : 0);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) t.setIdTecnico(rs.getInt(1));
            }
        }
    }

    public List<Tecnico> listarTodos() throws SQLException {
        String sql = "SELECT * FROM tecnico ORDER BY nome";
        List<Tecnico> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        }
        return lista;
    }

    public Tecnico buscarPorId(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM tecnico WHERE id_tecnico=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public void atualizar(Tecnico t) throws SQLException {
        String sql = "UPDATE tecnico SET nome=?,email=?,login=?,ativo=? WHERE id_tecnico=?";
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, t.getNome());
            ps.setString(2, t.getEmail());
            ps.setString(3, t.getLogin());
            ps.setInt   (4, t.isAtivo() ? 1 : 0);
            ps.setInt   (5, t.getIdTecnico());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM tecnico WHERE id_tecnico=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Tecnico mapear(ResultSet rs) throws SQLException {
        return new Tecnico(
            rs.getInt    ("id_tecnico"),
            rs.getString ("nome"),
            rs.getString ("email"),
            rs.getString ("login"),
            rs.getInt    ("ativo") == 1
        );
    }
}
