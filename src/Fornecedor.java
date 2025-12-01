public class Fornecedor {

    private static int ultimoID = 10_000; 

    private String nome;
    private int documento;
    private Lista<Produto> produtos;

    public Fornecedor(String nome) {
        if (nome == null || nome.trim().split("\\s+").length < 2) {
            throw new IllegalArgumentException("O nome do fornecedor deve conter pelo menos duas palavras.");
        }

        this.nome = nome;
        this.documento = ultimoID++; 
        this.produtos = new Lista<>(); 
    }


    public void adicionarProduto(Produto novo) {
        if (novo == null) {
            throw new IllegalArgumentException("Produto inválido."); 
        }
        this.produtos.inserirFinal(novo);
    }


    @Override
    public int hashCode() {
        return this.documento; 
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fornecedor outro = (Fornecedor) obj;
        return this.documento == outro.documento;
    }

    @Override
    public String toString() { 
        StringBuilder sb = new StringBuilder();
        sb.append("Fornecedor: ").append(nome)
          .append(" | Documento: ").append(documento).append("\n");
        sb.append("Produtos fornecidos:\n");
        if(produtos.vazia()){
            sb.append(" - Nenhum produto registrado.\n");
        } else {
            sb.append(produtos.toString());
        }
        return sb.toString();
    }
    
    public int getDocumento() {
        return documento;
    }
}