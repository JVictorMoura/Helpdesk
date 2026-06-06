package helpdesk.service;

import helpdesk.dao.ConnectionFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Collectors;

/**
 * Inicializa o banco de dados na primeira execução,
 * lendo o schema.sql incluído no classpath (pasta db/).
 */
public class DatabaseInitializer {

    public static void inicializar() {
        try (Connection c = ConnectionFactory.getConnection()) {
            // Verificar se a tabela usuario já existe
            DatabaseMetaData meta = c.getMetaData();
            try (ResultSet rs = meta.getTables(null, null, "usuario", null)) {
                if (rs.next()) return; // banco já inicializado
            }
            // Ler schema.sql do classpath
            InputStream is = DatabaseInitializer.class
                    .getClassLoader().getResourceAsStream("schema.sql");
            if (is == null) {
                System.err.println("[AVISO] schema.sql não encontrado no classpath. Banco vazio.");
                return;
            }
            String sql = new BufferedReader(new InputStreamReader(is))
                    .lines().collect(Collectors.joining("\n"));
            // Executar cada statement separado por ";"
            try (Statement st = c.createStatement()) {
                for (String stmt : sql.split(";")) {
                    String s = stmt.strip();
                    if (!s.isEmpty() && !s.startsWith("--"))
                        st.execute(s);
                }
            }
            System.out.println("[OK] Banco de dados inicializado com sucesso.");
        } catch (Exception e) {
            System.err.println("[ERRO] Falha ao inicializar banco: " + e.getMessage());
        }
    }
}
