package helpdesk.model;

public class Tecnico {
    private int    idTecnico;
    private String nome;
    private String email;
    private String login;
    private boolean ativo;

    public Tecnico() {}

    public Tecnico(int idTecnico, String nome, String email, String login, boolean ativo) {
        this.idTecnico = idTecnico;
        this.nome      = nome;
        this.email     = email;
        this.login     = login;
        this.ativo     = ativo;
    }

    public int     getIdTecnico()   { return idTecnico; }
    public void    setIdTecnico(int v){ this.idTecnico = v; }
    public String  getNome()        { return nome; }
    public void    setNome(String v){ this.nome = v; }
    public String  getEmail()       { return email; }
    public void    setEmail(String v){ this.email = v; }
    public String  getLogin()       { return login; }
    public void    setLogin(String v){ this.login = v; }
    public boolean isAtivo()        { return ativo; }
    public void    setAtivo(boolean v){ this.ativo = v; }

    @Override public String toString() { return nome + " [" + login + "]"; }
}
