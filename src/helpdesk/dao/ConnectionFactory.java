package helpdesk.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Fornece conexão JDBC para o banco SQLite.
 * O arquivo helpdesk.db é criado na pasta de execução.
 */
public class ConnectionFactory {

    private static final String URL = "jdbc:sqlite:helpdesk.db";

    private ConnectionFactory() {}

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver SQLite não encontrado. Adicione sqlite-jdbc ao classpath.", e);
        }
        Connection conn = DriverManager.getConnection(URL);
        // Ativar foreign keys no SQLite (desabilitadas por padrão)
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        return conn;
    }
}
