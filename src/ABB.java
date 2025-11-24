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
	
	/**
	 * Método auxiliar para inicialização da árvore binária de busca.
	 */
	private void init(Comparator<K> comparador) {
		raiz = null;
		tamanho = 0;
		this.comparador = comparador;
	}

	@SuppressWarnings("unchecked")
	public ABB() {
	    init((Comparator<K>) Comparator.naturalOrder());
	}

	public ABB(Comparator<K> comparador) {
	    init(comparador);
	}

    public ABB(ABB<?, V> original, Function<V, K> funcaoChave) {
        ABB<K, V> nova = new ABB<>();
        nova = copiarArvore(original.raiz, funcaoChave, nova);
        this.raiz = nova.raiz;
    }
    
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
    
	public Boolean vazia() {
	    return (this.raiz == null);
	}
    
    @Override
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
    		throw new NoSuchElementException("O item não foi localizado na árvore!");
    	
    	comparacao = comparador.compare(procurado, raizArvore.getChave());
    	
    	if (comparacao == 0)
    		return raizArvore.getItem();
    	else if (comparacao < 0)
    		return pesquisar(raizArvore.getEsquerda(), procurado);
    	else
    		return pesquisar(raizArvore.getDireita(), procurado);
    }
    
    @Override
    public int inserir(K chave, V item) {
        this.raiz = inserir(this.raiz, chave, item);
        tamanho++;
        return tamanho;
    }

    protected No<K, V> inserir(No<K, V> raizArvore, K chave, V item) {
    	
    	int comparacao;
    	
        if (raizArvore == null)
            raizArvore = new No<>(chave, item);
        else {
        	comparacao = comparador.compare(chave, raizArvore.getChave());
        
        	if (comparacao < 0)
        		raizArvore.setEsquerda(inserir(raizArvore.getEsquerda(), chave, item));
        	else if (comparacao > 0)
        		raizArvore.setDireita(inserir(raizArvore.getDireita(), chave, item));
        	else
        		throw new IllegalArgumentException("O item já foi inserido anteriormente na árvore.");
        }
        return raizArvore;
    }

    @Override 
    public String toString(){
    	return percorrer();
    }

    @Override
    public String percorrer() {
    	return caminhamentoEmOrdem();
    }

    public String caminhamentoEmOrdem() {
    	
    	if (vazia())
    		throw new IllegalStateException("A árvore está vazia!");
    	
    	return caminhamentoEmOrdem(raiz);
    }

    private String caminhamentoEmOrdem(No<K, V> raizArvore) {
    	if (raizArvore != null) {
    		String resposta = caminhamentoEmOrdem(raizArvore.getEsquerda());
    		resposta += raizArvore.getItem() + "\n";
    		resposta += caminhamentoEmOrdem(raizArvore.getDireita());
    		
    		return resposta;
    	} else {
    		return "";
    	}
    }

    @Override
    public V remover(K chave) {
    	
    	V removido = pesquisar(chave);
    	raiz = remover(raiz, chave);
    	tamanho--;
    	return removido;
    }

    protected No<K, V> remover(No<K, V> raizArvore, K chaveRemover) {
    	
    	int comparacao;
    	
        if (raizArvore == null) 
        	throw new NoSuchElementException("O item a ser removido não foi localizado na árvore!");
        
        comparacao = comparador.compare(chaveRemover, raizArvore.getChave());
        
        if (comparacao == 0) {
        	if (raizArvore.getDireita() == null) {
                raizArvore = raizArvore.getEsquerda();
        	} else if (raizArvore.getEsquerda() == null) {
                raizArvore = raizArvore.getDireita();
        	} else {
                raizArvore.setEsquerda(removerNoAntecessor(raizArvore, raizArvore.getEsquerda()));
        	}
        } else if (comparacao < 0)
            raizArvore.setEsquerda(remover(raizArvore.getEsquerda(), chaveRemover));
        else
            raizArvore.setDireita(remover(raizArvore.getDireita(), chaveRemover));
         
        return raizArvore;
    }

    protected No<K, V> removerNoAntecessor(No<K, V> itemRetirar, No<K, V> raizArvore) {
        if (raizArvore.getDireita() != null) {
            raizArvore.setDireita(removerNoAntecessor(itemRetirar, raizArvore.getDireita()));
        } else {
        	itemRetirar.setChave(raizArvore.getChave());
            itemRetirar.setItem(raizArvore.getItem());
            raizArvore = raizArvore.getEsquerda();
        }
        return raizArvore;
    }

    public Lista<V> recortar(K chaveDeOnde, K chaveAteOnde) {
        Lista<V> listaResultado = new Lista<>();
        recortar(raiz, chaveDeOnde, chaveAteOnde, listaResultado);
        return listaResultado;
    }
    
    private void recortar(No<K, V> no, K min, K max, Lista<V> lista) {
        if (no == null) {
            return;
        }

        int comparaMin = comparador.compare(no.getChave(), min);
        int comparaMax = comparador.compare(no.getChave(), max);

        // Se a chave do nó atual é maior que o mínimo, pode haver elementos válidos à esquerda.
        if (comparaMin > 0) {
            recortar(no.getEsquerda(), min, max, lista);
        }

        // Se a chave do nó atual está dentro do intervalo (min <= chave <= max), adiciona à lista.
        if (comparaMin >= 0 && comparaMax <= 0) {
            lista.inserir(no.getItem());
        }

        // Se a chave do nó atual é menor que o máximo, pode haver elementos válidos à direita.
        if (comparaMax < 0) {
            recortar(no.getDireita(), min, max, lista);
        }
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
		return (termino - inicio) / 1_000_000.0; // Ajuste para double se necessário, ou manter como long
	}
}