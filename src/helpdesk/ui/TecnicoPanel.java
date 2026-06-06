package helpdesk.ui;

import helpdesk.dao.TecnicoDAO;
import helpdesk.model.Tecnico;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class TecnicoPanel extends JPanel {

    private final TecnicoDAO dao = new TecnicoDAO();
    private final DefaultTableModel tableModel;
    private final JTable table;

    private final JTextField txtNome  = new JTextField(20);
    private final JTextField txtEmail = new JTextField(20);
    private final JTextField txtLogin = new JTextField(14);
    private final JCheckBox  chkAtivo = new JCheckBox("Ativo", true);
    private       int        idSel    = -1;

    public TecnicoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] cols = {"ID", "Nome", "E-mail", "Login", "Ativo"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.setRowHeight(22);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) preencher();
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Dados do Técnico"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6); g.anchor = GridBagConstraints.WEST;

        int r = 0;
        for (String[] pair : new String[][]{{"Nome *",""},{"E-mail *",""},{"Login *",""}}) {
            g.gridx = 0; g.gridy = r; form.add(new JLabel(pair[0]), g);
            g.gridx = 1;
            JTextField f = r == 0 ? txtNome : r == 1 ? txtEmail : txtLogin;
            form.add(f, g); r++;
        }
        g.gridx = 1; g.gridy = r; form.add(chkAtivo, g);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        JButton bSalvar = new JButton("💾  Salvar");
        JButton bNovo   = new JButton("➕  Novo");
        JButton bExcluir= new JButton("🗑  Excluir");
        bSalvar .addActionListener(e -> salvar());
        bNovo   .addActionListener(e -> limpar());
        bExcluir.addActionListener(e -> excluir());
        btns.add(bNovo); btns.add(bSalvar); btns.add(bExcluir);
        g.gridx = 0; g.gridy = r+1; g.gridwidth = 2; form.add(btns, g);

        add(form, BorderLayout.EAST);
        carregar();
    }

    private void carregar() {
        tableModel.setRowCount(0);
        try {
            for (Tecnico t : dao.listarTodos())
                tableModel.addRow(new Object[]{
                    t.getIdTecnico(), t.getNome(), t.getEmail(), t.getLogin(), t.isAtivo()?"Sim":"Não"
                });
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void preencher() {
        int row = table.getSelectedRow(); if (row < 0) return;
        idSel = (int) tableModel.getValueAt(row, 0);
        try {
            Tecnico t = dao.buscarPorId(idSel); if (t == null) return;
            txtNome .setText(t.getNome());
            txtEmail.setText(t.getEmail());
            txtLogin.setText(t.getLogin());
            chkAtivo.setSelected(t.isAtivo());
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void salvar() {
        if (txtNome.getText().isBlank() || txtEmail.getText().isBlank() || txtLogin.getText().isBlank()) {
            JOptionPane.showMessageDialog(this, "Preencha Nome, E-mail e Login.", "Validação", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Tecnico t = new Tecnico(idSel < 0 ? 0 : idSel,
                txtNome.getText().trim(), txtEmail.getText().trim(),
                txtLogin.getText().trim(), chkAtivo.isSelected());
            if (idSel < 0) dao.inserir(t); else dao.atualizar(t);
            limpar(); carregar();
            JOptionPane.showMessageDialog(this, "Técnico salvo!");
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void excluir() {
        if (idSel < 0) { JOptionPane.showMessageDialog(this, "Selecione um técnico."); return; }
        if (JOptionPane.showConfirmDialog(this,"Confirmar exclusão?","Excluir",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { dao.excluir(idSel); limpar(); carregar(); }
        catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void limpar() {
        idSel = -1; txtNome.setText(""); txtEmail.setText(""); txtLogin.setText("");
        chkAtivo.setSelected(true); table.clearSelection();
    }

    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }
}
