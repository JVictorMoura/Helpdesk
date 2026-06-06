package helpdesk.dao;

import helpdesk.model.Categoria;
import java.sql.*;
import java.util.*;

public class CategoriaDAO {

    public void inserir(Categoria cat) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "INSERT INTO categoria (nome, descricao) VALUES (?,?)",
                 Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, cat.getNome());
            ps.setString(2, cat.getDescricao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) cat.setIdCategoria(rs.getInt(1));
            }
        }
    }

    public List<Categoria> listarTodos() throws SQLException {
        List<Categoria> lista = new ArrayList<>();
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM categoria ORDER BY nome");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next())
                lista.add(new Categoria(rs.getInt("id_categoria"), rs.getString("nome"), rs.getString("descricao")));
        }
        return lista;
    }

    public void atualizar(Categoria cat) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE categoria SET nome=?,descricao=? WHERE id_categoria=?")) {
            ps.setString(1, cat.getNome());
            ps.setString(2, cat.getDescricao());
            ps.setInt   (3, cat.getIdCategoria());
            ps.executeUpdate();
        }
    }

    public void excluir(int id) throws SQLException {
        try (Connection c = ConnectionFactory.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM categoria WHERE id_categoria=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
