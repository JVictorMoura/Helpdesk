package helpdesk.ui;

import helpdesk.service.DatabaseInitializer;

import javax.swing.*;
import java.awt.*;

/**
 * Janela principal do sistema Helpdesk de TI.
 * Organiza as telas em abas (JTabbedPane).
 */
public class MainFrame extends JFrame {

    public MainFrame() {
        super("Helpdesk de TI — UCB");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1050, 650);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(900, 550));

        // ── Cabeçalho ─────────────────────────────────────
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 10));
        header.setBackground(new Color(30, 60, 114));
        JLabel titulo = new JLabel("🖥  Helpdesk de TI");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titulo.setForeground(Color.WHITE);
        header.add(titulo);
        add(header, BorderLayout.NORTH);

        // ── Abas ──────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        tabs.addTab("📋  Chamados",  new ChamadoPanel());
        tabs.addTab("👤  Usuários",  new UsuarioPanel());
        tabs.addTab("🔧  Técnicos",  new TecnicoPanel());

        add(tabs, BorderLayout.CENTER);

        // ── Rodapé ────────────────────────────────────────
        JLabel rodape = new JLabel(
            "  Laboratório de Banco de Dados — UCB 2025  |  SQLite + JDBC + Swing",
            SwingConstants.CENTER);
        rodape.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        rodape.setForeground(Color.GRAY);
        rodape.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));
        rodape.setPreferredSize(new Dimension(0, 24));
        add(rodape, BorderLayout.SOUTH);
    }

    // ── Ponto de entrada ─────────────────────────────────
    public static void main(String[] args) {
        // Inicializar banco (cria tabelas se necessário)
        DatabaseInitializer.inicializar();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
