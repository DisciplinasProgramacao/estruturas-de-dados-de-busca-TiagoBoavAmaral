import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class ABB<K, V> implements IMapeamento<K, V>{

	private No<K, V> raiz; // referência à raiz da árvore.
	private Comparator<K> comparador; //comparador empregado para definir "menores" e "maiores".
	private int tamanho;
	private long comparacoes;
	private long inicio;
	private long termino;
    private V valorRemovidoCache; // Auxiliar para o método remover
	
	/**
	 * Método auxiliar para inicialização da árvore binária de busca.
	 * * Este método define a raiz da árvore como {@code null} e seu tamanho como 0.
	 * Utiliza o comparador fornecido para definir a organização dos elementos na árvore.
	 * @param comparador o comparador para organizar os elementos da árvore.
	 */
	private void init(Comparator<K> comparador) {
		raiz = null;
		tamanho = 0;
		this.comparador = comparador;
        valorRemovidoCache = null;
	}

	/**
	 * Construtor da classe.
	 * O comparador padrão de ordem natural será utilizado.
	 */ 
	@SuppressWarnings("unchecked")
	public ABB() {
	    init((Comparator<K>) Comparator.naturalOrder());
	}

	/**
	 * Construtor da classe.
	 * Esse construtor cria uma nova árvore binária de busca vazia.
	 * * @param comparador o comparador a ser utilizado para organizar os elementos da árvore.  
	 */
	public ABB(Comparator<K> comparador) {
	    init(comparador);
	}

    /**
     * Construtor da classe.
     * Esse construtor cria uma nova árvore binária a partir de uma outra árvore binária de busca,
     * com os mesmos itens, mas usando uma nova chave.
     * @param original a árvore binária de busca original.
     * @param funcaoChave a função que irá extrair a nova chave de cada item para a nova árvore.
     */
    public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
        ABB<K, V> nova = new ABB<>();
        nova = copiarArvore(original.raiz, funcaoChave, nova);
        this.raiz = nova.raiz;
    }
    
    /**
     * Recursivamente, copia os elementos da árvore original para esta, num processo análogo ao caminhamento em ordem.
     * @param <T> Tipo da nova chave.
     * @param raizArvore raiz da árvore original que será copiada.
     * @param funcaoChave função extratora da nova chave para cada item da árvore.
     * @param novaArvore Nova árvore. Parâmetro usado para permitir o retorno da recursividade.
     * @return A nova árvore com os itens copiados e usando a chave indicada pela função extratora.
     */
    private <T> ABB<T, V> copiarArvore(No<?, V> raizArvore, Function<V, T> funcaoChave, ABB<T, V> novaArvore) {
    	
        if (raizArvore != null) {
    		novaArvore = copiarArvore(raizArvore.getEsquerda(), funcaoChave, novaArvore);
            V item = raizArvore.getItem();
            T chave = funcaoChave.apply(item);
    		novaArvore.inserir(chave, item);
    		novaArvore = copiarArvore(raizArvore.getDireita(), funcaoChave, novaArvore);
    	}
        return novaArvore;
    }
    
    /**
	 * Método booleano que indica se a árvore está vazia ou não.
	 * @return
	 * verdadeiro: se a raiz da árvore for null, o que significa que a árvore está vazia.
	 * falso: se a raiz da árvore não for null, o que significa que a árvore não está vazia.
	 */
	public Boolean vazia() {
	    return (this.raiz == null);
	}
    
    @Override
    /**
     * Método que encapsula a pesquisa recursiva de itens na árvore.
     * @param chave a chave do item que será pesquisado na árvore.
     * @return o valor associado à chave.
     */
	public V pesquisar(K chave) {
    	comparacoes = 0;
    	inicio = System.nanoTime();
    	V procurado = pesquisar(raiz, chave);
    	termino = System.nanoTime();
    	return procurado;
	}
    
    private V pesquisar(No<K, V> raizArvore, K procurado) {
    	
    	int comparacao;
    	
    	comparacoes++;
    	if (raizArvore == null)
    		/// Se a raiz da árvore ou sub-árvore for null, a árvore/sub-árvore está vazia e então o item não foi encontrado.
    		throw new NoSuchElementException("O item não foi localizado na árvore!");
    	
    	comparacao = comparador.compare(procurado, raizArvore.getChave());
    	
    	if (comparacao == 0)
    		/// O item procurado foi encontrado.
    		return raizArvore.getItem();
    	else if (comparacao < 0)
    		/// Se o item procurado for menor do que o item armazenado na raiz da árvore:
            /// pesquise esse item na sub-árvore esquerda.    
    		return pesquisar(raizArvore.getEsquerda(), procurado);
    	else
    		/// Se o item procurado for maior do que o item armazenado na raiz da árvore:
            /// pesquise esse item na sub-árvore direita.
    		return pesquisar(raizArvore.getDireita(), procurado);
    }
    
    @Override
    /**
     * Método que encapsula a adição recursiva de itens à árvore, associando-o à chave fornecida.
     * @param chave a chave associada ao item que será inserido na árvore.
     * @param item o item que será inserido na árvore.
     * * @return o tamanho atualizado da árvore após a execução da operação de inserção.
     */
    public int inserir(K chave, V item) {
    	this.raiz = inserirRecursivo(this.raiz, chave, item);
        return tamanho;
    }

    /**
     * Método auxiliar recursivo para inserir um novo nó na árvore.
     * @param raizAtual O nó raiz da subárvore atual.
     * @param chave A chave do novo nó.
     * @param item O valor (item) do novo nó.
     * @return O nó raiz da subárvore modificada.
     */
    private No<K, V> inserirRecursivo(No<K, V> raizAtual, K chave, V item) {
        if (raizAtual == null) {
            this.tamanho++;
            return new No<>(chave, item);
        }

        int comparacao = comparador.compare(chave, raizAtual.getChave());

        if (comparacao < 0) {
            raizAtual.setEsquerda(inserirRecursivo(raizAtual.getEsquerda(), chave, item));
        } else if (comparacao > 0) {
            raizAtual.setDireita(inserirRecursivo(raizAtual.getDireita(), chave, item));
        } else {
            // Chave já existe, apenas atualiza o valor.
            raizAtual.setItem(item);
        }
        
        raizAtual.setAltura(); // Atualiza a altura do nó
        return raizAtual;
    }

    @Override 
    public String toString(){
    	return percorrer();
    }

    @Override
    public String percorrer() {
    	return caminhamentoEmOrdem();
    }

    /**
     * Retorna uma representação em String da árvore realizando um caminhamento em ordem.
     * @return String com os itens da árvore em ordem.
     */
    public String caminhamentoEmOrdem() {
        StringBuilder sb = new StringBuilder();
        caminhamentoEmOrdem(raiz, sb);
        return sb.toString();
    }

    /**
     * Método auxiliar recursivo para o caminhamento em ordem (In-order traversal).
     * @param raizArvore O nó raiz da subárvore atual.
     * @param sb O StringBuilder para construir a string de resultado.
     */
    private void caminhamentoEmOrdem(No<K, V> raizArvore, StringBuilder sb) {
        if (raizArvore != null) {
            caminhamentoEmOrdem(raizArvore.getEsquerda(), sb);
            // Adiciona o toString() do item (Produto)
            sb.append(raizArvore.getItem().toString()).append("\n"); 
            caminhamentoEmOrdem(raizArvore.getDireita(), sb);
        }
    }

    @Override
    /**
     * Método que encapsula a remoção recursiva de um item da árvore.
     * @param chave a chave do item que deverá ser localizado e removido da árvore.
     * @return o valor associado ao item removido, or null se a chave não for encontrada.
     */
    public V remover(K chave) {
        valorRemovidoCache = null; // Limpa o cache
        raiz = removerRecursivo(raiz, chave);
        
        if (valorRemovidoCache != null) {
            tamanho--; // Decrementa o tamanho apenas se a remoção ocorreu
        }
        
        return valorRemovidoCache; // Retorna o valor do item removido
    }

    /**
     * Método auxiliar recursivo para remover um nó da árvore.
     * @param raizAtual O nó raiz da subárvore atual.
     * @param chave A chave do nó a ser removido.
     * @return O nó raiz da subárvore modificada.
     */
    private No<K, V> removerRecursivo(No<K, V> raizAtual, K chave) {
        if (raizAtual == null) {
            return null; // Chave não encontrada
        }

        int comparacao = comparador.compare(chave, raizAtual.getChave());

        if (comparacao < 0) {
            raizAtual.setEsquerda(removerRecursivo(raizAtual.getEsquerda(), chave));
        } else if (comparacao > 0) {
            raizAtual.setDireita(removerRecursivo(raizAtual.getDireita(), chave));
        } else {
            // Nó encontrado.
            valorRemovidoCache = raizAtual.getItem(); // Armazena o valor para retorno

            // Caso 1: Nó é uma folha (sem filhos)
            if (raizAtual.getEsquerda() == null && raizAtual.getDireita() == null) {
                return null; // Remove o nó
            }
            
            // Caso 2: Nó tem apenas um filho
            if (raizAtual.getEsquerda() == null) {
                return raizAtual.getDireita(); // Substitui pelo filho direito
            }
            if (raizAtual.getDireita() == null) {
                return raizAtual.getEsquerda(); // Substitui pelo filho esquerdo
            }

            // Caso 3: Nó tem dois filhos
            // Encontrar o sucessor (o menor nó da subárvore direita)
            No<K, V> sucessor = encontrarMinimo(raizAtual.getDireita());
            
            // Copiar os dados do sucessor para este nó
            raizAtual.setChave(sucessor.getChave());
            raizAtual.setItem(sucessor.getItem());
            
            // Remover o sucessor da subárvore direita
            raizAtual.setDireita(removerRecursivo(raizAtual.getDireita(), sucessor.getChave()));
        }
        
        // Atualiza a altura (necessário após a remoção)
        if (raizAtual != null) {
            raizAtual.setAltura();
        }
        
        return raizAtual;
    }

    /**
     * Método auxiliar para encontrar o nó com a menor chave em uma subárvore.
     * @param no O nó raiz da subárvore.
     * @return O nó com a menor chave.
     */
    private No<K, V> encontrarMinimo(No<K, V> no) {
        while (no.getEsquerda() != null) {
            no = no.getEsquerda();
        }
        return no;
    }

	@Override
	public int tamanho() {
		return tamanho;
	}
	
	@Override
	public long getComparacoes() {
		return comparacoes;
	}

	@Override
	public double getTempo() {
        // Convertendo nanosegundos para milissegundos
		return (termino - inicio) / 1_000_000.0;
	}
}