package br.com.precify.app;

import br.com.precify.model.CategoriaProduto;
import br.com.precify.model.Insumo;
import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;
import br.com.precify.repository.ArquivoProdutoRepository;
import br.com.precify.repository.ProdutoRepository;
import br.com.precify.service.CalculadoraDePreco;
import br.com.precify.service.CalculadoraPrecificacao;
import br.com.precify.service.RelatorioService;
import br.com.precify.ui.JOptionPaneUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.SwingUtilities;

public class PrecifyApp {
    private static final String TITULO = "Precify 2.0";

    private final List<Produto> produtos = new ArrayList<>();
    private final CalculadoraDePreco calculadora = new CalculadoraPrecificacao();
    private final ProdutoRepository produtoRepository;
    private final RelatorioService relatorioService;
    private final Path arquivoRelatorio;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public PrecifyApp() {
        Path pastaDados = Path.of(System.getProperty("user.dir"), "data");
        this.produtoRepository = new ArquivoProdutoRepository(pastaDados.resolve("produtos.txt"));
        this.relatorioService = new RelatorioService(calculadora);
        this.arquivoRelatorio = pastaDados.resolve("relatorio_precify.txt");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PrecifyApp().iniciar());
    }

    private void iniciar() {
        carregarProdutos();

        boolean executando = true;
        while (executando) {
            String opcao = JOptionPaneUtils.escolherOpcao(
                    TITULO,
                    "Escolha uma acao:",
                    new String[] {
                        "Cadastrar produto",
                        "Listar produtos",
                        "Consultar detalhes",
                        "Editar produto",
                        "Remover produto",
                        "Gerar relatorio",
                        "Sair"
                    });

            if (opcao == null || "Sair".equals(opcao)) {
                executando = false;
                continue;
            }

            switch (opcao) {
                case "Cadastrar produto" -> cadastrarProduto();
                case "Listar produtos" -> listarProdutos();
                case "Consultar detalhes" -> consultarDetalhes();
                case "Editar produto" -> editarProduto();
                case "Remover produto" -> removerProduto();
                case "Gerar relatorio" -> gerarRelatorio();
                default -> JOptionPaneUtils.mostrarErro("Opcao invalida.");
            }
        }

        JOptionPaneUtils.mostrarMensagem(TITULO, "Precify 2.0 encerrado.");
    }

    private void carregarProdutos() {
        try {
            produtos.clear();
            produtos.addAll(produtoRepository.carregar());
        } catch (IOException erro) {
            JOptionPaneUtils.mostrarErro("Nao foi possivel carregar os produtos: " + erro.getMessage());
        }
    }

    private void cadastrarProduto() {
        try {
            String nome = JOptionPaneUtils.solicitarTexto(TITULO, "Nome do produto:");
            if (nome == null) {
                return;
            }

            CategoriaProduto categoria = escolherCategoria();
            if (categoria == null) {
                return;
            }

            Double rendimento = JOptionPaneUtils.solicitarDouble(TITULO, "Rendimento total do lote:", false);
            if (rendimento == null) {
                return;
            }

            String unidadeRendimento = JOptionPaneUtils.solicitarTexto(
                    TITULO,
                    "Unidade do rendimento (porcoes, unidades, kg, litros):");
            if (unidadeRendimento == null) {
                return;
            }

            Double percentualGastos = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Percentual de gastos indiretos (%):",
                    true);
            if (percentualGastos == null) {
                return;
            }

            Double percentualLucro = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Percentual de lucro desejado (%):",
                    true);
            if (percentualLucro == null) {
                return;
            }

            Double custoEmbalagem = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Custo de embalagem por unidade (use 0 se nao houver):",
                    true);
            if (custoEmbalagem == null) {
                return;
            }

            Produto produto = new Produto(
                    nome,
                    categoria,
                    rendimento,
                    unidadeRendimento,
                    percentualGastos,
                    percentualLucro,
                    custoEmbalagem);

            adicionarInsumos(produto);
            produtos.add(produto);
            persistirProdutos();

            JOptionPaneUtils.mostrarMensagem(
                    TITULO,
                    "Produto cadastrado com sucesso.\nPreco sugerido por unidade: "
                            + moeda.format(calculadora.calcularPrecoSugeridoUnitario(produto)));
        } catch (IllegalArgumentException erro) {
            JOptionPaneUtils.mostrarErro(erro.getMessage());
        }
    }

    private void adicionarInsumos(Produto produto) {
        while (JOptionPaneUtils.confirmar(TITULO, "Deseja adicionar um insumo ao produto?")) {
            String nomeInsumo = JOptionPaneUtils.solicitarTexto(TITULO, "Nome do insumo:");
            if (nomeInsumo == null) {
                return;
            }

            String unidadeInsumo = JOptionPaneUtils.solicitarTexto(TITULO, "Unidade do insumo (g, ml, un, kg):");
            if (unidadeInsumo == null) {
                return;
            }

            Double quantidadeEmbalagem = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Quantidade da embalagem do insumo:",
                    false);
            if (quantidadeEmbalagem == null) {
                return;
            }

            Double precoEmbalagem = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Preco da embalagem do insumo:",
                    true);
            if (precoEmbalagem == null) {
                return;
            }

            Double quantidadeUsada = JOptionPaneUtils.solicitarDouble(
                    TITULO,
                    "Quantidade usada no lote:",
                    true);
            if (quantidadeUsada == null) {
                return;
            }

            Insumo insumo = new Insumo(nomeInsumo, unidadeInsumo, quantidadeEmbalagem, precoEmbalagem);
            produto.adicionarItemCusto(new ItemCusto(insumo, quantidadeUsada));
        }
    }

    private void listarProdutos() {
        if (produtos.isEmpty()) {
            JOptionPaneUtils.mostrarMensagem(TITULO, "Nenhum produto cadastrado.");
            return;
        }

        StringBuilder resumo = new StringBuilder("Produtos cadastrados:\n\n");
        for (int i = 0; i < produtos.size(); i++) {
            Produto produto = produtos.get(i);
            resumo.append(i + 1)
                    .append(". ")
                    .append(produto.getNome())
                    .append(" - ")
                    .append(moeda.format(calculadora.calcularPrecoSugeridoUnitario(produto)))
                    .append(" por ")
                    .append(produto.getUnidadeRendimento())
                    .append('\n');
        }

        JOptionPaneUtils.mostrarMensagem(TITULO, resumo.toString());
    }

    private void consultarDetalhes() {
        Produto produto = selecionarProduto();
        if (produto == null) {
            return;
        }

        StringBuilder detalhes = new StringBuilder();
        detalhes.append("Produto: ").append(produto.getNome()).append('\n');
        detalhes.append("Categoria: ").append(produto.getCategoria().getDescricao()).append('\n');
        detalhes.append("Rendimento: ").append(produto.getRendimento()).append(' ')
                .append(produto.getUnidadeRendimento()).append('\n');
        detalhes.append("Gastos indiretos: ").append(produto.getPercentualGastosIndiretos()).append("%\n");
        detalhes.append("Lucro: ").append(produto.getPercentualLucro()).append("%\n");
        detalhes.append("Embalagem por unidade: ").append(moeda.format(produto.getCustoEmbalagemUnitario())).append('\n');
        detalhes.append("Custo de materiais do lote: ")
                .append(moeda.format(calculadora.calcularCustoMateriais(produto))).append('\n');
        detalhes.append("Custo total do lote: ")
                .append(moeda.format(calculadora.calcularCustoTotalLote(produto))).append('\n');
        detalhes.append("Preco sugerido por unidade: ")
                .append(moeda.format(calculadora.calcularPrecoSugeridoUnitario(produto))).append("\n\n");
        detalhes.append("Insumos:\n");

        if (!produto.possuiItens()) {
            detalhes.append("- Nenhum insumo cadastrado.");
        } else {
            for (ItemCusto item : produto.getItensCusto()) {
                detalhes.append("- ")
                        .append(item.getInsumo().getNome())
                        .append(": usado ")
                        .append(item.getQuantidadeUsada())
                        .append(' ')
                        .append(item.getInsumo().getUnidadeMedida())
                        .append(" | custo ")
                        .append(moeda.format(item.calcularCusto()))
                        .append('\n');
            }
        }

        JOptionPaneUtils.mostrarMensagem(TITULO, detalhes.toString());
    }

    private void editarProduto() {
        Produto produto = selecionarProduto();
        if (produto == null) {
            return;
        }

        boolean editando = true;
        while (editando) {
            String opcao = JOptionPaneUtils.escolherOpcao(
                    TITULO,
                    "Editando: " + produto.getNome(),
                    new String[] {
                        "Alterar nome",
                        "Alterar categoria",
                        "Alterar rendimento",
                        "Alterar percentuais",
                        "Alterar embalagem",
                        "Adicionar insumo",
                        "Remover insumo",
                        "Voltar"
                    });

            if (opcao == null || "Voltar".equals(opcao)) {
                return;
            }

            try {
                switch (opcao) {
                    case "Alterar nome" -> {
                        String novoNome = JOptionPaneUtils.solicitarTexto(TITULO, "Novo nome do produto:");
                        if (novoNome != null) {
                            produto.setNome(novoNome);
                        }
                    }
                    case "Alterar categoria" -> {
                        CategoriaProduto categoria = escolherCategoria();
                        if (categoria != null) {
                            produto.setCategoria(categoria);
                        }
                    }
                    case "Alterar rendimento" -> {
                        Double rendimento = JOptionPaneUtils.solicitarDouble(TITULO, "Novo rendimento:", false);
                        if (rendimento != null) {
                            produto.setRendimento(rendimento);
                        }
                        String unidade = JOptionPaneUtils.solicitarTexto(TITULO, "Nova unidade de rendimento:");
                        if (unidade != null) {
                            produto.setUnidadeRendimento(unidade);
                        }
                    }
                    case "Alterar percentuais" -> {
                        Double gastos = JOptionPaneUtils.solicitarDouble(TITULO, "Novo percentual de gastos:", true);
                        Double lucro = JOptionPaneUtils.solicitarDouble(TITULO, "Novo percentual de lucro:", true);
                        if (gastos != null && lucro != null) {
                            produto.setPercentualGastosIndiretos(gastos);
                            produto.setPercentualLucro(lucro);
                        }
                    }
                    case "Alterar embalagem" -> {
                        Double embalagem = JOptionPaneUtils.solicitarDouble(
                                TITULO,
                                "Novo custo de embalagem por unidade:",
                                true);
                        if (embalagem != null) {
                            produto.setCustoEmbalagemUnitario(embalagem);
                        }
                    }
                    case "Adicionar insumo" -> adicionarInsumos(produto);
                    case "Remover insumo" -> removerInsumo(produto);
                    default -> editando = false;
                }

                persistirProdutos();
            } catch (IllegalArgumentException erro) {
                JOptionPaneUtils.mostrarErro(erro.getMessage());
            }
        }
    }

    private void removerInsumo(Produto produto) {
        if (!produto.possuiItens()) {
            JOptionPaneUtils.mostrarMensagem(TITULO, "Este produto nao possui insumos.");
            return;
        }

        String[] nomes = produto.getItensCusto().stream()
                .map(item -> item.getInsumo().getNome())
                .toArray(String[]::new);

        String escolhido = JOptionPaneUtils.escolherOpcao(TITULO, "Escolha o insumo para remover:", nomes);
        if (escolhido == null) {
            return;
        }

        for (int i = 0; i < produto.getItensCusto().size(); i++) {
            if (produto.getItensCusto().get(i).getInsumo().getNome().equals(escolhido)) {
                produto.removerItemCusto(i);
                return;
            }
        }
    }

    private void removerProduto() {
        Produto produto = selecionarProduto();
        if (produto == null) {
            return;
        }

        if (JOptionPaneUtils.confirmar(TITULO, "Remover o produto " + produto.getNome() + "?")) {
            produtos.remove(produto);
            persistirProdutos();
            JOptionPaneUtils.mostrarMensagem(TITULO, "Produto removido.");
        }
    }

    private void gerarRelatorio() {
        try {
            relatorioService.gerar(arquivoRelatorio, produtos);
            JOptionPaneUtils.mostrarMensagem(
                    TITULO,
                    "Relatorio gerado com sucesso em:\n" + arquivoRelatorio);
        } catch (IOException erro) {
            JOptionPaneUtils.mostrarErro("Falha ao gerar relatorio: " + erro.getMessage());
        }
    }

    private void persistirProdutos() {
        try {
            produtoRepository.salvar(produtos);
            relatorioService.gerar(arquivoRelatorio, produtos);
        } catch (IOException erro) {
            JOptionPaneUtils.mostrarErro("Falha ao salvar dados: " + erro.getMessage());
        }
    }

    private Produto selecionarProduto() {
        if (produtos.isEmpty()) {
            JOptionPaneUtils.mostrarMensagem(TITULO, "Nenhum produto cadastrado.");
            return null;
        }

        String[] nomes = produtos.stream().map(Produto::getNome).toArray(String[]::new);
        String escolhido = JOptionPaneUtils.escolherOpcao(TITULO, "Selecione um produto:", nomes);
        if (escolhido == null) {
            return null;
        }

        for (Produto produto : produtos) {
            if (produto.getNome().equals(escolhido)) {
                return produto;
            }
        }
        return null;
    }

    private CategoriaProduto escolherCategoria() {
        String[] opcoes = new String[CategoriaProduto.values().length];
        for (int i = 0; i < CategoriaProduto.values().length; i++) {
            opcoes[i] = CategoriaProduto.values()[i].getDescricao();
        }

        String escolha = JOptionPaneUtils.escolherOpcao(TITULO, "Categoria do produto:", opcoes);
        if (escolha == null) {
            return null;
        }

        for (CategoriaProduto categoria : CategoriaProduto.values()) {
            if (categoria.getDescricao().equals(escolha)) {
                return categoria;
            }
        }
        return null;
    }
}
