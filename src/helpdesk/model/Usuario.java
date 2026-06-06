package helpdesk.model;

public class Usuario {
    private int    idUsuario;
    private String nome;
    private String email;
    private String departamento;
    private String telefone;
    private boolean ativo;

    public Usuario() {}

    public Usuario(int idUsuario, String nome, String email,
                   String departamento, String telefone, boolean ativo) {
        this.idUsuario    = idUsuario;
        this.nome         = nome;
        this.email        = email;
        this.departamento = departamento;
        this.telefone     = telefone;
        this.ativo        = ativo;
    }

    // ── Getters & Setters ──────────────────────────────────
    public int     getIdUsuario()    { return idUsuario; }
    public void    setIdUsuario(int v){ this.idUsuario = v; }
    public String  getNome()         { return nome; }
    public void    setNome(String v) { this.nome = v; }
    public String  getEmail()        { return email; }
    public void    setEmail(String v){ this.email = v; }
    public String  getDepartamento() { return departamento; }
    public void    setDepartamento(String v){ this.departamento = v; }
    public String  getTelefone()     { return telefone; }
    public void    setTelefone(String v){ this.telefone = v; }
    public boolean isAtivo()         { return ativo; }
    public void    setAtivo(boolean v){ this.ativo = v; }

    @Override public String toString() { return nome + " (" + departamento + ")"; }
}
