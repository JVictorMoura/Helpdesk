package helpdesk.ui;

import helpdesk.dao.UsuarioDAO;
import helpdesk.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class UsuarioPanel extends JPanel {

    private final UsuarioDAO dao = new UsuarioDAO();

    // Tabela
    private final DefaultTableModel tableModel;
    private final JTable table;

    // Campos
    private final JTextField txtNome         = new JTextField(20);
    private final JTextField txtEmail        = new JTextField(20);
    private final JTextField txtDepto        = new JTextField(15);
    private final JTextField txtTelefone     = new JTextField(14);
    private final JCheckBox  chkAtivo        = new JCheckBox("Ativo", true);
    private       int        idSelecionado    = -1;

    public UsuarioPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Tabela ────────────────────────────────────────
        String[] cols = {"ID", "Nome", "E-mail", "Departamento", "Telefone", "Ativo"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setRowHeight(22);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencherFormulario();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Formulário ────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do Usuário"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;

        addField(form, g, 0, "Nome *",        txtNome);
        addField(form, g, 1, "E-mail *",      txtEmail);
        addField(form, g, 2, "Departamento *",txtDepto);
        addField(form, g, 3, "Telefone",      txtTelefone);
        g.gridx = 0; g.gridy = 4; form.add(new JLabel(""), g);
        g.gridx = 1; form.add(chkAtivo, g);

        // ── Botões ────────────────────────────────────────
        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton btnSalvar  = new JButton("💾  Salvar");
        JButton btnNovo    = new JButton("➕  Novo");
        JButton btnExcluir = new JButton("🗑  Excluir");

        btnSalvar .addActionListener(e -> salvar());
        btnNovo   .addActionListener(e -> limpar());
        btnExcluir.addActionListener(e -> excluir());

        botoes.add(btnNovo); botoes.add(btnSalvar); botoes.add(btnExcluir);
        g.gridx = 0; g.gridy = 5; g.gridwidth = 2;
        form.add(botoes, g);

        add(form, BorderLayout.EAST);
        carregarTabela();
    }

    // ── Helpers de layout ─────────────────────────────────
    private void addField(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.gridwidth = 1;
        p.add(new JLabel(label), g);
        g.gridx = 1;
        p.add(field, g);
    }

    // ── Lógica ────────────────────────────────────────────
    private void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            for (Usuario u : dao.listarTodos())
                tableModel.addRow(new Object[]{
                    u.getIdUsuario(), u.getNome(), u.getEmail(),
                    u.getDepartamento(), u.getTelefone(), u.isAtivo() ? "Sim" : "Não"
                });
        } catch (Exception ex) {
            mostrarErro(ex.getMessage());
        }
    }

    private void preencherFormulario() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        idSelecionado = (int) tableModel.getValueAt(row, 0);
        try {
            Usuario u = dao.buscarPorId(idSelecionado);
            if (u == null) return;
            txtNome    .setText(u.getNome());
            txtEmail   .setText(u.getEmail());
            txtDepto   .setText(u.getDepartamento());
            txtTelefone.setText(u.getTelefone());
            chkAtivo   .setSelected(u.isAtivo());
        } catch (Exception ex) { mostrarErro(ex.getMessage()); }
    }

    private void salvar() {
        String nome  = txtNome .getText().trim();
        String email = txtEmail.getText().trim();
        String depto = txtDepto.getText().trim();
        if (nome.isEmpty() || email.isEmpty() || depto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Campos obrigatórios: Nome, E-mail e Departamento.",
                    "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Usuario u = new Usuario(idSelecionado < 0 ? 0 : idSelecionado,
                    nome, email, depto, txtTelefone.getText().trim(), chkAtivo.isSelected());
            if (idSelecionado < 0) dao.inserir(u);
            else                   dao.atualizar(u);
            limpar();
            carregarTabela();
            JOptionPane.showMessageDialog(this, "Usuário salvo com sucesso!");
        } catch (Exception ex) { mostrarErro(ex.getMessage()); }
    }

    private void excluir() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this, "Selecione um usuário."); return; }
        int ok = JOptionPane.showConfirmDialog(this, "Confirmar exclusão?", "Excluir",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.excluir(idSelecionado);
            limpar();
            carregarTabela();
        } catch (Exception ex) { mostrarErro(ex.getMessage()); }
    }

    private void limpar() {
        idSelecionado = -1;
        txtNome.setText(""); txtEmail.setText("");
        txtDepto.setText(""); txtTelefone.setText("");
        chkAtivo.setSelected(true);
        table.clearSelection();
    }

    private void mostrarErro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
