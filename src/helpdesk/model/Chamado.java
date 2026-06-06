package helpdesk.model;

public class Chamado {
    public enum Prioridade { BAIXA, MEDIA, ALTA, CRITICA }
    public enum Status     { ABERTO, EM_ATENDIMENTO, RESOLVIDO, FECHADO }

    private int       idChamado;
    private String    titulo;
    private String    descricao;
    private Prioridade prioridade;
    private Status    status;
    private String    dtAbertura;
    private String    dtFechamento;
    private int       idUsuario;
    private String    nomeUsuario;   // desnormalizado para listagem
    private int       idCategoria;
    private String    nomeCategoria; // desnormalizado para listagem
    private Integer   idEquipamento; // nullable
    private String    descEquipamento;

    public Chamado() {
        this.prioridade = Prioridade.MEDIA;
        this.status     = Status.ABERTO;
    }

    // ── Getters & Setters ──────────────────────────────────
    public int        getIdChamado()         { return idChamado; }
    public void       setIdChamado(int v)    { this.idChamado = v; }
    public String     getTitulo()            { return titulo; }
    public void       setTitulo(String v)    { this.titulo = v; }
    public String     getDescricao()         { return descricao; }
    public void       setDescricao(String v) { this.descricao = v; }
    public Prioridade getPrioridade()        { return prioridade; }
    public void       setPrioridade(Prioridade v){ this.prioridade = v; }
    public Status     getStatus()            { return status; }
    public void       setStatus(Status v)    { this.status = v; }
    public String     getDtAbertura()        { return dtAbertura; }
    public void       setDtAbertura(String v){ this.dtAbertura = v; }
    public String     getDtFechamento()      { return dtFechamento; }
    public void       setDtFechamento(String v){ this.dtFechamento = v; }
    public int        getIdUsuario()         { return idUsuario; }
    public void       setIdUsuario(int v)    { this.idUsuario = v; }
    public String     getNomeUsuario()       { return nomeUsuario; }
    public void       setNomeUsuario(String v){ this.nomeUsuario = v; }
    public int        getIdCategoria()       { return idCategoria; }
    public void       setIdCategoria(int v)  { this.idCategoria = v; }
    public String     getNomeCategoria()     { return nomeCategoria; }
    public void       setNomeCategoria(String v){ this.nomeCategoria = v; }
    public Integer    getIdEquipamento()     { return idEquipamento; }
    public void       setIdEquipamento(Integer v){ this.idEquipamento = v; }
    public String     getDescEquipamento()   { return descEquipamento; }
    public void       setDescEquipamento(String v){ this.descEquipamento = v; }

    @Override public String toString() {
        return "[#" + idChamado + "] " + titulo + " — " + status;
    }
}
