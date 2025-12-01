import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    static String nomeArquivoDados;
    static Scanner teclado;
    static int quantosProdutos = 0;


    static AVL<String, Produto> produtosBalanceadosPorNome;
    static AVL<Integer, Produto> produtosBalanceadosPorId;
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;

    static AVL<Integer, Fornecedor> fornecedoresPorID;
    static TabelaHash<Produto, Lista<Fornecedor>> fornecedoresDoProduto;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("\nDigite enter para continuar...");
        teclado.nextLine();
    }

    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
   
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        T valor;
        System.out.print(mensagem);
        try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    static int menu() {
        cabecalho();
        System.out.println("1 - Procurar produto, por id");
        System.out.println("2 - Relatório: Pedidos de um produto (Arquivo)");
        System.out.println("3 - Relatório: Dados de um Fornecedor (Tela)"); 
        System.out.println("4 - Relatório: Fornecedores de um Produto (Arquivo)"); 
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        
        try {
            return Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    static <K> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        AVL<K, Produto> produtosCadastrados;
        K chave;
        
        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new AVL<K, Produto>();
            
            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                chave = extratorDeChave.apply(produto);
                produtosCadastrados.inserir(chave, produto);
            }
            quantosProdutos = numProdutos;
            
        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            if (arquivo != null) arquivo.close();
        }
        return produtosCadastrados;
    }

    static <K> AVL<K, Fornecedor> lerFornecedores(String nomeArquivo, Function<Fornecedor, K> extratorDeChave) {
        Scanner arquivo = null;
        AVL<K, Fornecedor> arvoreFornecedores = new AVL<>();
        Random sorteio = new Random(42); 

        try {
            arquivo = new Scanner(new File(nomeArquivo), Charset.forName("UTF-8"));
            int qtdFornecedores = Integer.parseInt(arquivo.nextLine()); 

            for (int i = 0; i < qtdFornecedores; i++) {
                String nome = arquivo.nextLine();
                try {
                    Fornecedor novoFornecedor = new Fornecedor(nome);
                    int qtdProdutos = sorteio.nextInt(7);
                    
                    for (int j = 0; j < qtdProdutos; j++) {
                        int idSorteado = 10_000 + sorteio.nextInt(quantosProdutos);
                        
                        try {
                            Produto prod = produtosBalanceadosPorId.pesquisar(idSorteado);
                            
                            novoFornecedor.adicionarProduto(prod);

                            Lista<Fornecedor> listaFornecedores;
                            try {
                                listaFornecedores = fornecedoresDoProduto.pesquisar(prod);
                            } catch (NoSuchElementException e) {
                                listaFornecedores = new Lista<>();
                                fornecedoresDoProduto.inserir(prod, listaFornecedores);
                            }
                            listaFornecedores.inserirFinal(novoFornecedor);

                        } catch (NoSuchElementException e) {
                        }
                    }


                    K chave = extratorDeChave.apply(novoFornecedor);
                    arvoreFornecedores.inserir(chave, novoFornecedor);

                } catch (IllegalArgumentException e) {
                    System.out.println("Erro ao criar fornecedor '" + nome + "': " + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println("Erro ao ler arquivo de fornecedores: " + e.getMessage());
            return null;
        } finally {
            if (arquivo != null) arquivo.close();
        }

        return arvoreFornecedores;
    }
    
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
        Produto produto;
        cabecalho();
        System.out.println("Localizando produto...");
        try {
            produto = produtosCadastrados.pesquisar(procurado);
            System.out.println("Produto encontrado: " + produto.toString());
        } catch (NoSuchElementException excecao) {
            produto = null;
            System.out.println("Produto não encontrado.");
        }
        return produto;
    }
    
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        Integer idProduto = lerOpcao("Digite o ID do produto: ", Integer.class);
        if (idProduto == null) return null;
        return localizarProduto(produtosCadastrados, idProduto);
    }
    
    private static void mostrarProduto(Produto produto) {

        if(produto == null) System.out.println("Operação cancelada.");
    }
    
    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int quantProdutos;
        int formaDePagamento;
        for (int i = 0; i < quantidade; i++) {
            formaDePagamento = sorteio.nextInt(2) + 1;
            Pedido pedido = new Pedido(LocalDate.now(), formaDePagamento);
            quantProdutos = sorteio.nextInt(8) + 1;
            for (int j = 0; j < quantProdutos; j++) {
                int id = sorteio.nextInt(quantosProdutos) + 10_000;
                try {
                    Produto produto = produtosBalanceadosPorId.pesquisar(id);
                    pedido.incluirProduto(produto);
                    inserirNaTabela(produto, pedido);
                } catch (NoSuchElementException e) {}
            }
            pedidos.inserirFinal(pedido);
        }
        return pedidos;
    }
    
    private static void inserirNaTabela(Produto produto, Pedido pedido) {
        Lista<Pedido> listaDePedidos;
        try {
            listaDePedidos = pedidosPorProduto.pesquisar(produto);
        } catch (NoSuchElementException excecao) {
            listaDePedidos = new Lista<>();
            pedidosPorProduto.inserir(produto, listaDePedidos);
        }
        listaDePedidos.inserirFinal(pedido);
    }
    
    static void pedidosDoProduto() {
        Produto produto = localizarProdutoID(produtosBalanceadosPorId);
        if (produto == null) return;
        
        Lista<Pedido> listaDePedidos;
        String nomeArquivo = "RelatorioPedidos_" + produto.hashCode() + ".txt";  
        
        try {
            listaDePedidos = pedidosPorProduto.pesquisar(produto);
            try (FileWriter arquivoRelatorio = new FileWriter(nomeArquivo, Charset.forName("UTF-8"))) {
                arquivoRelatorio.write("RELATÓRIO DE PEDIDOS\n");
                arquivoRelatorio.write("Produto: " + produto.toString() + "\n");
                arquivoRelatorio.write("========================================\n");
                arquivoRelatorio.write(listaDePedidos.toString() + "\n");
                System.out.println("Relatório salvo em: " + nomeArquivo);
            } catch(IOException excecao) {
                System.out.println("Erro ao gravar arquivo: " + excecao.getMessage());        	
            }
        } catch (NoSuchElementException e) {
            System.out.println("Nenhum pedido encontrado para este produto.");
        }
    }

    static void relatorioDeFornecedor() {
        cabecalho();
        Integer id = lerOpcao("Digite o Documento (ID) do fornecedor: ", Integer.class);
        if (id == null) return;

        try {
            Fornecedor f = fornecedoresPorID.pesquisar(id);
            System.out.println("\n=== DADOS DO FORNECEDOR ===");
            System.out.println(f.toString());
        } catch (NoSuchElementException e) {
            System.out.println("Fornecedor com documento " + id + " não encontrado.");
        }
    }


    static void fornecedoresDoProduto() {
        cabecalho();
        System.out.println("Gerar relatório de fornecedores de um produto.");
        Produto p = localizarProdutoID(produtosBalanceadosPorId);
        
        if (p == null) return;

        String nomeArquivo = "FornecedoresDoProduto_" + p.hashCode() + ".txt";

        try {
            Lista<Fornecedor> lista = fornecedoresDoProduto.pesquisar(p);
            
            try (FileWriter fw = new FileWriter(nomeArquivo, Charset.forName("UTF-8"))) {
                fw.write("RELATÓRIO DE FORNECEDORES\n");
                fw.write("Produto: " + p.toString() + "\n");
                fw.write("========================================\n");
                fw.write(lista.toString()); 
                System.out.println("Relatório gerado com sucesso: " + nomeArquivo);
            } catch (IOException e) {
                System.out.println("Erro ao escrever arquivo: " + e.getMessage());
            }

        } catch (NoSuchElementException e) {
            System.out.println("Este produto não possui fornecedores cadastrados.");
        }
    }
    
    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        String nomeArquivoFornecedores = "fornecedores.txt";
        
        System.out.println("Carregando produtos...");
        produtosBalanceadosPorId = lerProdutos(nomeArquivoDados, Produto::hashCode);
        
        if (produtosBalanceadosPorId == null) {
            System.out.println("Erro: Arquivo 'produtos.txt' não encontrado.");
            return;
        }

        produtosBalanceadosPorNome = new AVL<>(produtosBalanceadosPorId, produto -> produto.descricao, String::compareTo);
        pedidosPorProduto = new TabelaHash<>((int)(quantosProdutos * 1.5));
        

        fornecedoresDoProduto = new TabelaHash<>((int)(quantosProdutos * 1.5));

        System.out.println("Gerando pedidos aleatórios...");
        gerarPedidos(1000); 

        System.out.println("Carregando fornecedores...");
 
        fornecedoresPorID = lerFornecedores(nomeArquivoFornecedores, Fornecedor::getDocumento);

        if (fornecedoresPorID == null) {
             System.out.println("Erro: Arquivo 'fornecedores.txt' não encontrado na raiz.");
        }
       
        int opcao = -1;
      
        do {
            opcao = menu();
            switch (opcao) {
                case 1 -> mostrarProduto(localizarProdutoID(produtosBalanceadosPorId));
                case 2 -> pedidosDoProduto();
                case 3 -> relatorioDeFornecedor(); 
                case 4 -> fornecedoresDoProduto();
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida!");
            }
            if (opcao != 0) pausa();
        } while(opcao != 0);       

        teclado.close();    
    }
}