package helpdesk.model;

public class Categoria {
    private int    idCategoria;
    private String nome;
    private String descricao;

    public Categoria() {}
    public Categoria(int id, String nome, String descricao) {
        this.idCategoria = id;
        this.nome        = nome;
        this.descricao   = descricao;
    }

    public int    getIdCategoria()      { return idCategoria; }
    public void   setIdCategoria(int v) { this.idCategoria = v; }
    public String getNome()             { return nome; }
    public void   setNome(String v)     { this.nome = v; }
    public String getDescricao()        { return descricao; }
    public void   setDescricao(String v){ this.descricao = v; }

    @Override public String toString() { return nome; }
}
