import java.nio.file.*;
import java.sql.*;

public class InitDB {
    public static void main(String[] args) throws Exception {
        String sql = Files.readString(Path.of("db/schema.sql"));
        Connection conn = DriverManager.getConnection("jdbc:sqlite:helpdesk.db");
        conn.createStatement().execute("PRAGMA foreign_keys = ON");
        
        StringBuilder stmt = new StringBuilder();
        for (String line : sql.split("\n")) {
            String trimmed = line.strip();
            if (trimmed.startsWith("--") || trimmed.isEmpty()) continue;
            // remove comentarios inline
            int commentIdx = trimmed.indexOf("--");
            if (commentIdx >= 0) trimmed = trimmed.substring(0, commentIdx).strip();
            stmt.append(trimmed).append(" ");
            if (trimmed.endsWith(";")) {
                String s = stmt.toString().strip();
                if (!s.equals(";"))
                    conn.createStatement().execute(s);
                stmt = new StringBuilder();
            }
        }
        System.out.println("Banco inicializado com sucesso!");
        conn.close();
    }
}