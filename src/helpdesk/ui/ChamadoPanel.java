package helpdesk.ui;

import helpdesk.dao.CategoriaDAO;
import helpdesk.dao.ChamadoDAO;
import helpdesk.dao.TecnicoDAO;
import helpdesk.dao.UsuarioDAO;
import helpdesk.model.Categoria;
import helpdesk.model.Chamado;
import helpdesk.model.Chamado.Prioridade;
import helpdesk.model.Chamado.Status;
import helpdesk.model.Tecnico;
import helpdesk.model.Usuario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ChamadoPanel extends JPanel {

    private final ChamadoDAO  chamadoDAO  = new ChamadoDAO();
    private final UsuarioDAO  usuarioDAO  = new UsuarioDAO();
    private final CategoriaDAO catDAO     = new CategoriaDAO();
    private final TecnicoDAO  tecnicoDAO  = new TecnicoDAO();

    // Tabela principal
    private final DefaultTableModel tableModel;
    private final JTable            table;

    // Formulário
    private final JTextField  txtTitulo  = new JTextField(24);
    private final JTextArea   txtDesc    = new JTextArea(3, 24);
    private final JComboBox<Prioridade> cbPrioridade = new JComboBox<>(Prioridade.values());
    private final JComboBox<Status>     cbStatus     = new JComboBox<>(Status.values());
    private final JComboBox<ComboItem>  cbUsuario    = new JComboBox<>();
    private final JComboBox<ComboItem>  cbCategoria  = new JComboBox<>();
    private       int idSelecionado = -1;

    // Técnicos (N:N)
    private final JComboBox<ComboItem> cbTecnico    = new JComboBox<>();
    private final JTextArea            txtObsTec    = new JTextArea(2, 18);
    private final DefaultListModel<String> tecListModel = new DefaultListModel<>();
    private final JList<String>           tecList       = new JList<>(tecListModel);

    // Filtro
    private final JComboBox<String> cbFiltroStatus = new JComboBox<>(
            new String[]{"TODOS","ABERTO","EM_ATENDIMENTO","RESOLVIDO","FECHADO"});

    public ChamadoPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // ── Tabela ────────────────────────────────────────
        String[] cols = {"ID","Título","Prioridade","Status","Abertura","Usuário","Categoria","Equipamento"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.setRowHeight(22);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) selecionarChamado();
        });

        // ── Filtro ────────────────────────────────────────
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        topBar.add(new JLabel("Filtrar por status:"));
        topBar.add(cbFiltroStatus);
        JButton btnFiltrar = new JButton("🔍 Filtrar");
        btnFiltrar.addActionListener(e -> carregarTabela());
        topBar.add(btnFiltrar);

        JPanel tabelaPanel = new JPanel(new BorderLayout());
        tabelaPanel.add(topBar, BorderLayout.NORTH);
        tabelaPanel.add(new JScrollPane(table), BorderLayout.CENTER);
        add(tabelaPanel, BorderLayout.CENTER);

        // ── Painel direito ────────────────────────────────
        JPanel right = new JPanel(new BorderLayout(6, 6));

        // Formulário
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Chamado"));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(3,5,3,5); g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, g, 0, "Título *",     txtTitulo);
        txtDesc.setLineWrap(true); txtDesc.setWrapStyleWord(true);
        addRow(form, g, 1, "Descrição *",  new JScrollPane(txtDesc));
        addRow(form, g, 2, "Prioridade",   cbPrioridade);
        addRow(form, g, 3, "Status",       cbStatus);
        addRow(form, g, 4, "Usuário *",    cbUsuario);
        addRow(form, g, 5, "Categoria *",  cbCategoria);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton bNovo    = new JButton("➕ Novo");
        JButton bSalvar  = new JButton("💾 Salvar");
        JButton bExcluir = new JButton("🗑 Excluir");
        bNovo   .addActionListener(e -> limpar());
        bSalvar .addActionListener(e -> salvar());
        bExcluir.addActionListener(e -> excluir());
        btns.add(bNovo); btns.add(bSalvar); btns.add(bExcluir);
        g.gridx=0; g.gridy=6; g.gridwidth=2; form.add(btns, g);

        right.add(form, BorderLayout.NORTH);

        // Painel técnicos (N:N)
        JPanel tecPanel = new JPanel(new GridBagLayout());
        tecPanel.setBorder(BorderFactory.createTitledBorder("Técnicos Responsáveis (N:N)"));
        GridBagConstraints gt = new GridBagConstraints();
        gt.insets = new Insets(3,5,3,5); gt.anchor = GridBagConstraints.WEST;
        gt.fill = GridBagConstraints.HORIZONTAL;

        gt.gridx=0; gt.gridy=0; tecPanel.add(new JLabel("Técnico:"), gt);
        gt.gridx=1; tecPanel.add(cbTecnico, gt);
        gt.gridx=0; gt.gridy=1; tecPanel.add(new JLabel("Observação:"), gt);
        gt.gridx=1; txtObsTec.setLineWrap(true);
        tecPanel.add(new JScrollPane(txtObsTec), gt);

        JPanel btTec = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        JButton bAtribuir = new JButton("➕ Atribuir");
        JButton bRemover  = new JButton("➖ Remover");
        bAtribuir.addActionListener(e -> atribuirTecnico());
        bRemover .addActionListener(e -> removerTecnico());
        btTec.add(bAtribuir); btTec.add(bRemover);
        gt.gridx=0; gt.gridy=2; gt.gridwidth=2; tecPanel.add(btTec, gt);

        gt.gridy=3;
        tecPanel.add(new JLabel("Técnicos no chamado:"), gt);
        gt.gridy=4;
        tecList.setVisibleRowCount(3);
        tecPanel.add(new JScrollPane(tecList), gt);

        right.add(tecPanel, BorderLayout.CENTER);
        right.setPreferredSize(new Dimension(360, 0));
        add(right, BorderLayout.EAST);

        // Carregar combos e tabela
        carregarCombos();
        carregarTabela();
    }

    // ── Helpers de layout ─────────────────────────────────
    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent c) {
        g.gridwidth=1; g.gridx=0; g.gridy=row; p.add(new JLabel(label), g);
        g.gridx=1; p.add(c, g);
    }

    // ── Lógica ────────────────────────────────────────────
    private void carregarCombos() {
        try {
            cbUsuario.removeAllItems();
            for (Usuario u : usuarioDAO.listarTodos())
                cbUsuario.addItem(new ComboItem(u.getIdUsuario(), u.toString()));
            cbCategoria.removeAllItems();
            for (Categoria cat : catDAO.listarTodos())
                cbCategoria.addItem(new ComboItem(cat.getIdCategoria(), cat.getNome()));
            cbTecnico.removeAllItems();
            for (Tecnico t : tecnicoDAO.listarTodos())
                cbTecnico.addItem(new ComboItem(t.getIdTecnico(), t.toString()));
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void carregarTabela() {
        tableModel.setRowCount(0);
        try {
            String filtro = (String) cbFiltroStatus.getSelectedItem();
            List<Chamado> lista = "TODOS".equals(filtro)
                    ? chamadoDAO.listarTodos()
                    : chamadoDAO.listarPorStatus(Status.valueOf(filtro));
            for (Chamado ch : lista)
                tableModel.addRow(new Object[]{
                    ch.getIdChamado(), ch.getTitulo(), ch.getPrioridade(),
                    ch.getStatus(), ch.getDtAbertura(),
                    ch.getNomeUsuario(), ch.getNomeCategoria(), ch.getDescEquipamento()
                });
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void selecionarChamado() {
        int row = table.getSelectedRow(); if (row < 0) return;
        idSelecionado = (int) tableModel.getValueAt(row, 0);
        try {
            Chamado ch = chamadoDAO.buscarPorId(idSelecionado); if (ch == null) return;
            txtTitulo.setText(ch.getTitulo());
            txtDesc  .setText(ch.getDescricao());
            cbPrioridade.setSelectedItem(ch.getPrioridade());
            cbStatus    .setSelectedItem(ch.getStatus());
            // técnicos
            tecListModel.clear();
            for (String s : chamadoDAO.listarTecnicosDoChamado(idSelecionado))
                tecListModel.addElement(s);
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void salvar() {
        String titulo = txtTitulo.getText().trim();
        String desc   = txtDesc  .getText().trim();
        if (titulo.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Título e Descrição são obrigatórios.","Validação",JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (cbUsuario.getSelectedItem() == null || cbCategoria.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Selecione Usuário e Categoria.","Validação",JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Chamado ch = new Chamado();
            ch.setIdChamado  (idSelecionado < 0 ? 0 : idSelecionado);
            ch.setTitulo     (titulo);
            ch.setDescricao  (desc);
            ch.setPrioridade ((Prioridade) cbPrioridade.getSelectedItem());
            ch.setStatus     ((Status)     cbStatus    .getSelectedItem());
            ch.setIdUsuario  (((ComboItem) cbUsuario  .getSelectedItem()).id);
            ch.setIdCategoria(((ComboItem) cbCategoria.getSelectedItem()).id);
            if (idSelecionado < 0) chamadoDAO.inserir(ch);
            else                   chamadoDAO.atualizar(ch);
            limpar(); carregarTabela();
            JOptionPane.showMessageDialog(this, "Chamado salvo com sucesso!");
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void excluir() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this,"Selecione um chamado."); return; }
        if (JOptionPane.showConfirmDialog(this,"Confirmar exclusão do chamado #"+idSelecionado+"?",
                "Excluir",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
        try { chamadoDAO.excluir(idSelecionado); limpar(); carregarTabela(); }
        catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void atribuirTecnico() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this,"Selecione um chamado."); return; }
        ComboItem ct = (ComboItem) cbTecnico.getSelectedItem();
        if (ct == null) return;
        try {
            chamadoDAO.atribuirTecnico(idSelecionado, ct.id, txtObsTec.getText().trim());
            selecionarChamado(); carregarTabela();
        } catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void removerTecnico() {
        if (idSelecionado < 0) { JOptionPane.showMessageDialog(this,"Selecione um chamado."); return; }
        ComboItem ct = (ComboItem) cbTecnico.getSelectedItem(); if (ct == null) return;
        try { chamadoDAO.removerTecnico(idSelecionado, ct.id); selecionarChamado(); }
        catch (Exception ex) { erro(ex.getMessage()); }
    }

    private void limpar() {
        idSelecionado = -1;
        txtTitulo.setText(""); txtDesc.setText(""); txtObsTec.setText("");
        cbPrioridade.setSelectedIndex(1); cbStatus.setSelectedIndex(0);
        tecListModel.clear(); table.clearSelection();
    }

    private void erro(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Erro", JOptionPane.ERROR_MESSAGE);
    }

    // ── Classe auxiliar para combos ────────────────────────
    static class ComboItem {
        final int id; final String label;
        ComboItem(int id, String label) { this.id = id; this.label = label; }
        @Override public String toString() { return label; }
    }
}
