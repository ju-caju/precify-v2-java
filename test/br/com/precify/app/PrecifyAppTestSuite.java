package br.com.precify.app;

import br.com.precify.model.CategoriaProduto;
import br.com.precify.model.Insumo;
import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;
import br.com.precify.model.TipoArredondamento;
import br.com.precify.repository.ArquivoProdutoRepository;
import br.com.precify.repository.ProdutoRepository;
import br.com.precify.service.CalculadoraDePreco;
import br.com.precify.service.CalculadoraPrecificacao;
import br.com.precify.service.RelatorioService;
import br.com.precify.ui.DialogService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

public class PrecifyAppTestSuite {
    private static final double EPSILON = 0.000001;

    public static void main(String[] args) throws Exception {
        Locale.setDefault(Locale.of("pt", "BR"));

        List<TestCase> tests = List.of(
                new TestCase("insumo calcula custo e valida entradas", PrecifyAppTestSuite::testInsumoCalculaCustoEValidaEntradas),
                new TestCase("item de custo valida quantidade", PrecifyAppTestSuite::testItemCustoValidaQuantidade),
                new TestCase("produto valida campos e colecao", PrecifyAppTestSuite::testProdutoValidaCamposEColecao),
                new TestCase("calculadora cobre formula principal", PrecifyAppTestSuite::testCalculadoraHappyPathSemArredondamento),
                new TestCase("calculadora cobre arredondamentos", PrecifyAppTestSuite::testCalculadoraArredondamentos),
                new TestCase("repositorio salva e carrega formato atual", PrecifyAppTestSuite::testRepositorioFormatoAtual),
                new TestCase("repositorio carrega formato legado", PrecifyAppTestSuite::testRepositorioFormatoLegado),
                new TestCase("relatorio gera detalhes esperados", PrecifyAppTestSuite::testRelatorioService),
                new TestCase("app cobre fluxo feliz principal", PrecifyAppTestSuite::testAppFluxoPrincipal),
                new TestCase("app cobre bordas com repositorio vazio e cancelamentos", PrecifyAppTestSuite::testAppFluxosVaziosECancelamentos),
                new TestCase("app cobre edicao completa e opcao invalida", PrecifyAppTestSuite::testAppEdicaoCompletaEOpcaoInvalida));

        int executados = 0;
        for (TestCase test : tests) {
            test.run();
            executados++;
            System.out.println("[OK] " + test.nome());
        }

        System.out.println("Todos os testes passaram: " + executados);
    }

    private static void testInsumoCalculaCustoEValidaEntradas() {
        Insumo insumo = new Insumo("Farinha", "g", 1000, 12.0);
        assertDoubleEquals(3.0, insumo.calcularCusto(250), EPSILON, "Custo proporcional do insumo incorreto.");

        assertThrows(IllegalArgumentException.class, () -> new Insumo("", "g", 1000, 10), "Nome do insumo");
        assertThrows(IllegalArgumentException.class, () -> new Insumo("Farinha", "", 1000, 10), "Unidade");
        assertThrows(IllegalArgumentException.class, () -> new Insumo("Farinha", "g", 0, 10), "Quantidade da embalagem");
        assertThrows(IllegalArgumentException.class, () -> new Insumo("Farinha", "g", 1000, -1), "Preco da embalagem");
        assertThrows(IllegalArgumentException.class, () -> insumo.calcularCusto(-1), "Quantidade usada");
    }

    private static void testItemCustoValidaQuantidade() {
        Insumo insumo = new Insumo("Leite", "ml", 1000, 8.0);
        ItemCusto item = new ItemCusto(insumo, 250);
        assertDoubleEquals(2.0, item.calcularCusto(), EPSILON, "Custo inicial do item incorreto.");

        item.setQuantidadeUsada(500);
        assertDoubleEquals(4.0, item.calcularCusto(), EPSILON, "Atualizacao da quantidade usada nao refletiu no custo.");

        assertThrows(IllegalArgumentException.class, () -> new ItemCusto(null, 10), "Insumo obrigatorio");
        assertThrows(IllegalArgumentException.class, () -> new ItemCusto(insumo, -1), "Quantidade usada");
        assertThrows(IllegalArgumentException.class, () -> item.setQuantidadeUsada(-5), "Quantidade usada");
    }

    private static void testProdutoValidaCamposEColecao() {
        Produto produto = criarProdutoBase("Cookie", TipoArredondamento.SEM_ARREDONDAMENTO);
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Chocolate", "g", 1000, 20), 200));

        assertTrue(produto.possuiItens(), "Produto deveria ter itens.");
        assertEquals(1, produto.getItensCusto().size(), "Quantidade de itens inesperada.");

        assertThrows(UnsupportedOperationException.class, () -> produto.getItensCusto().add(null), null);
        assertThrows(IllegalArgumentException.class, () -> produto.setNome(" "), "Nome do produto");
        assertThrows(IllegalArgumentException.class, () -> produto.setCategoria(null), "Categoria");
        assertThrows(IllegalArgumentException.class, () -> produto.setRendimento(0), "Rendimento");
        assertThrows(IllegalArgumentException.class, () -> produto.setUnidadeRendimento(" "), "Unidade");
        assertThrows(IllegalArgumentException.class, () -> produto.setPercentualGastosIndiretos(-1), "Percentual de gastos");
        assertThrows(IllegalArgumentException.class, () -> produto.setPercentualDesperdicio(-1), "Percentual de desperdicio");
        assertThrows(IllegalArgumentException.class, () -> produto.setPercentualTaxaVenda(-1), "Percentual de taxa");
        assertThrows(IllegalArgumentException.class, () -> produto.setPercentualLucro(-1), "Percentual de lucro");
        assertThrows(IllegalArgumentException.class, () -> produto.setCustoEmbalagemUnitario(-1), "Custo da embalagem");
        assertThrows(IllegalArgumentException.class, () -> produto.setTipoArredondamento(null), "Tipo de arredondamento");
        assertThrows(IllegalArgumentException.class, () -> produto.adicionarItemCusto(null), "Item de custo");

        produto.removerItemCusto(0);
        assertFalse(produto.possuiItens(), "Produto nao deveria ter itens apos remocao.");
    }

    private static void testCalculadoraHappyPathSemArredondamento() {
        CalculadoraDePreco calculadora = new CalculadoraPrecificacao();
        Produto produto = new Produto(
                "Brownie",
                CategoriaProduto.SOBREMESA,
                10,
                "unidades",
                20,
                10,
                15,
                30,
                0.5,
                TipoArredondamento.SEM_ARREDONDAMENTO);

        produto.adicionarItemCusto(new ItemCusto(new Insumo("Chocolate", "g", 1000, 10.0), 500));
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Ovo", "un", 12, 12.0), 4));

        assertDoubleEquals(9.0, calculadora.calcularCustoMateriais(produto), EPSILON, "Custo de materiais incorreto.");
        assertDoubleEquals(5.0, calculadora.calcularCustoEmbalagemLote(produto), EPSILON, "Custo de embalagem do lote incorreto.");
        assertDoubleEquals(14.0, calculadora.calcularSubtotalBase(produto), EPSILON, "Subtotal base incorreto.");
        assertDoubleEquals(27.6276, calculadora.calcularCustoTotalLoteSemArredondamento(produto), EPSILON, "Custo total sem arredondamento incorreto.");
        assertDoubleEquals(2.76276, calculadora.calcularPrecoSugeridoUnitario(produto), EPSILON, "Preco unitario incorreto.");
    }

    private static void testCalculadoraArredondamentos() {
        CalculadoraDePreco calculadora = new CalculadoraPrecificacao();

        Produto base = criarProdutoComPrecoBase(2.76276, TipoArredondamento.SEM_ARREDONDAMENTO);
        Produto a10 = criarProdutoComPrecoBase(2.76276, TipoArredondamento.MULTIPLO_0_10);
        Produto a50 = criarProdutoComPrecoBase(2.76276, TipoArredondamento.MULTIPLO_0_50);
        Produto a100 = criarProdutoComPrecoBase(2.76276, TipoArredondamento.MULTIPLO_1_00);
        Produto final99 = criarProdutoComPrecoBase(2.76276, TipoArredondamento.FINAL_99);
        Produto final99Borda = criarProdutoComPrecoBase(2.995, TipoArredondamento.FINAL_99);

        assertDoubleEquals(2.76276, calculadora.calcularPrecoSugeridoUnitario(base), EPSILON, "Sem arredondamento incorreto.");
        assertDoubleEquals(2.8, calculadora.calcularPrecoSugeridoUnitario(a10), EPSILON, "Arredondamento 0,10 incorreto.");
        assertDoubleEquals(3.0, calculadora.calcularPrecoSugeridoUnitario(a50), EPSILON, "Arredondamento 0,50 incorreto.");
        assertDoubleEquals(3.0, calculadora.calcularPrecoSugeridoUnitario(a100), EPSILON, "Arredondamento 1,00 incorreto.");
        assertDoubleEquals(2.99, calculadora.calcularPrecoSugeridoUnitario(final99), EPSILON, "Arredondamento final ,99 incorreto.");
        assertDoubleEquals(3.99, calculadora.calcularPrecoSugeridoUnitario(final99Borda), EPSILON, "Arredondamento final ,99 de borda incorreto.");
    }

    private static void testRepositorioFormatoAtual() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-repo-atual");
        Path arquivo = tempDir.resolve("produtos.txt");
        ProdutoRepository repository = new ArquivoProdutoRepository(arquivo);

        Produto produto = new Produto(
                "Pao de mel",
                CategoriaProduto.SOBREMESA,
                12,
                "unidades",
                8,
                3,
                10,
                25,
                0.45,
                TipoArredondamento.FINAL_99);
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Chocolate 70%", "g", 1000, 32), 250));
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Leite condensado", "g", 395, 7.5), 395));

        repository.salvar(List.of(produto));
        List<Produto> carregados = repository.carregar();

        assertEquals(1, carregados.size(), "Quantidade de produtos carregados inesperada.");
        Produto carregado = carregados.get(0);
        assertEquals("Pao de mel", carregado.getNome(), "Nome carregado incorreto.");
        assertEquals(CategoriaProduto.SOBREMESA, carregado.getCategoria(), "Categoria carregada incorreta.");
        assertDoubleEquals(12, carregado.getRendimento(), EPSILON, "Rendimento carregado incorreto.");
        assertEquals("unidades", carregado.getUnidadeRendimento(), "Unidade carregada incorreta.");
        assertDoubleEquals(8, carregado.getPercentualGastosIndiretos(), EPSILON, "Gastos carregados incorretos.");
        assertDoubleEquals(3, carregado.getPercentualDesperdicio(), EPSILON, "Desperdicio carregado incorreto.");
        assertDoubleEquals(10, carregado.getPercentualTaxaVenda(), EPSILON, "Taxa carregada incorreta.");
        assertDoubleEquals(25, carregado.getPercentualLucro(), EPSILON, "Lucro carregado incorreto.");
        assertDoubleEquals(0.45, carregado.getCustoEmbalagemUnitario(), EPSILON, "Embalagem carregada incorreta.");
        assertEquals(TipoArredondamento.FINAL_99, carregado.getTipoArredondamento(), "Arredondamento carregado incorreto.");
        assertEquals(2, carregado.getItensCusto().size(), "Itens carregados inesperados.");
    }

    private static void testRepositorioFormatoLegado() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-repo-legado");
        Path arquivo = tempDir.resolve("produtos.txt");

        List<String> linhas = List.of(
                "PRODUTO|" + codificar("Bolo simples") + "|ALIMENTO|10.0|" + codificar("fatias") + "|5.0|20.0|0.5",
                "ITEM|" + codificar("Farinha") + "|" + codificar("g") + "|1000.0|8.0|500.0",
                "FIM");
        Files.write(arquivo, linhas, StandardCharsets.UTF_8);

        ProdutoRepository repository = new ArquivoProdutoRepository(arquivo);
        List<Produto> carregados = repository.carregar();

        assertEquals(1, carregados.size(), "Produto legado nao foi carregado.");
        Produto produto = carregados.get(0);
        assertEquals("Bolo simples", produto.getNome(), "Nome legado incorreto.");
        assertDoubleEquals(5.0, produto.getPercentualGastosIndiretos(), EPSILON, "Gastos legados incorretos.");
        assertDoubleEquals(0.0, produto.getPercentualDesperdicio(), EPSILON, "Desperdicio legado deveria ser zero.");
        assertDoubleEquals(0.0, produto.getPercentualTaxaVenda(), EPSILON, "Taxa legado deveria ser zero.");
        assertDoubleEquals(20.0, produto.getPercentualLucro(), EPSILON, "Lucro legado incorreto.");
        assertDoubleEquals(0.5, produto.getCustoEmbalagemUnitario(), EPSILON, "Embalagem legado incorreta.");
        assertEquals(TipoArredondamento.SEM_ARREDONDAMENTO, produto.getTipoArredondamento(), "Arredondamento legado deveria ser o padrao.");
        assertEquals(1, produto.getItensCusto().size(), "Item legado nao foi carregado.");
    }

    private static void testRelatorioService() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-relatorio");
        Path arquivo = tempDir.resolve("relatorio.txt");
        RelatorioService relatorioService = new RelatorioService(new CalculadoraPrecificacao());

        Produto produto = criarProdutoBase("Mousse", TipoArredondamento.MULTIPLO_0_10);
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Chocolate", "g", 1000, 30), 200));

        relatorioService.gerar(arquivo, List.of(produto));

        String conteudo = Files.readString(arquivo);
        assertContains(conteudo, "Relatorio de Precificacao", "Cabecalho do relatorio ausente.");
        assertContains(conteudo, "Produto: Mousse", "Produto nao apareceu no relatorio.");
        assertContains(conteudo, "Arredondamento: Para cima em R$ 0,10", "Arredondamento nao apareceu no relatorio.");
        assertContains(conteudo, "Chocolate", "Insumo nao apareceu no relatorio.");
    }

    private static void testAppFluxoPrincipal() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-app-principal");
        Path dados = tempDir.resolve("produtos.txt");
        Path relatorio = tempDir.resolve("relatorio.txt");

        ProdutoRepository repository = new ArquivoProdutoRepository(dados);
        RelatorioService relatorioService = new RelatorioService(new CalculadoraPrecificacao());
        FakeDialogService dialog = new FakeDialogService()
                .withOption("Cadastrar produto")
                .withText("Brownie")
                .withOption("Sobremesa")
                .withDouble(10.0)
                .withText("unidades")
                .withDouble(10.0)
                .withDouble(5.0)
                .withDouble(12.0)
                .withDouble(30.0)
                .withDouble(0.5)
                .withOption("Final ,99")
                .withConfirm(true)
                .withText("Chocolate")
                .withText("g")
                .withDouble(1000.0)
                .withDouble(30.0)
                .withDouble(200.0)
                .withConfirm(true)
                .withText("Ovo")
                .withText("un")
                .withDouble(12.0)
                .withDouble(12.0)
                .withDouble(3.0)
                .withConfirm(false)
                .withOption("Consultar detalhes")
                .withOption("Brownie")
                .withOption("Editar produto")
                .withOption("Brownie")
                .withOption("Alterar nome")
                .withText("Brownie premium")
                .withOption("Alterar embalagem")
                .withDouble(0.75)
                .withOption("Adicionar insumo")
                .withConfirm(true)
                .withText("Acucar")
                .withText("g")
                .withDouble(1000.0)
                .withDouble(8.0)
                .withDouble(100.0)
                .withConfirm(false)
                .withOption("Remover insumo")
                .withOption("Ovo")
                .withOption("Voltar")
                .withOption("Listar produtos")
                .withOption("Remover produto")
                .withOption("Brownie premium")
                .withConfirm(true)
                .withOption("Sair");

        PrecifyApp app = new PrecifyApp(repository, relatorioService, relatorio, dialog);
        app.iniciar();

        List<Produto> carregados = repository.carregar();
        assertEquals(0, carregados.size(), "Produto deveria ter sido removido ao final do fluxo.");

        assertTrue(dialog.containsMessage("Produto cadastrado com sucesso"), "Mensagem de cadastro nao encontrada.");
        assertTrue(dialog.containsMessage("Produto: Brownie"), "Consulta de detalhes nao foi exibida.");
        assertTrue(dialog.containsMessage("Brownie premium"), "Listagem nao refletiu a edicao.");
        assertTrue(dialog.containsMessage("Produto removido."), "Mensagem de remocao nao encontrada.");
        assertTrue(dialog.containsMessage("Precify 2.0 encerrado."), "Mensagem final de encerramento nao encontrada.");

        String conteudoRelatorio = Files.readString(relatorio);
        assertContains(conteudoRelatorio, "Nenhum produto cadastrado.", "Relatorio final deveria refletir lista vazia.");
    }

    private static void testAppFluxosVaziosECancelamentos() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-app-vazio");
        Path dados = tempDir.resolve("produtos.txt");
        Path relatorio = tempDir.resolve("relatorio.txt");

        ProdutoRepository repository = new ArquivoProdutoRepository(dados);
        RelatorioService relatorioService = new RelatorioService(new CalculadoraPrecificacao());
        FakeDialogService dialog = new FakeDialogService()
                .withOption("Listar produtos")
                .withOption("Consultar detalhes")
                .withOption("Editar produto")
                .withOption("Remover produto")
                .withOption("Gerar relatorio")
                .withOption("Cadastrar produto")
                .withNullText()
                .withOption("Sair");

        PrecifyApp app = new PrecifyApp(repository, relatorioService, relatorio, dialog);
        app.iniciar();

        assertEquals(0, repository.carregar().size(), "Cadastro cancelado nao deveria persistir produto.");
        assertTrue(dialog.countMessagesContaining("Nenhum produto cadastrado.") >= 3, "Mensagens de lista vazia insuficientes.");
        assertTrue(dialog.containsMessage("Relatorio gerado com sucesso"), "Geracao de relatorio vazio nao foi sinalizada.");

        String conteudoRelatorio = Files.readString(relatorio);
        assertContains(conteudoRelatorio, "Nenhum produto cadastrado.", "Relatorio vazio nao foi gerado corretamente.");
    }

    private static void testAppEdicaoCompletaEOpcaoInvalida() throws Exception {
        Path tempDir = Files.createTempDirectory("precify-app-edicao");
        Path dados = tempDir.resolve("produtos.txt");
        Path relatorio = tempDir.resolve("relatorio.txt");

        Produto produto = criarProdutoBase("Limonada", TipoArredondamento.SEM_ARREDONDAMENTO);
        produto.adicionarItemCusto(new ItemCusto(new Insumo("Limao", "un", 10, 8), 5));

        ProdutoRepository repository = new ArquivoProdutoRepository(dados);
        repository.salvar(List.of(produto));

        RelatorioService relatorioService = new RelatorioService(new CalculadoraPrecificacao());
        FakeDialogService dialog = new FakeDialogService()
                .withOption("Opcao desconhecida")
                .withOption("Editar produto")
                .withOption("Limonada")
                .withOption("Alterar categoria")
                .withOption("Bebida")
                .withOption("Alterar rendimento")
                .withDouble(2.0)
                .withText("litros")
                .withOption("Alterar percentuais")
                .withDouble(12.0)
                .withDouble(4.0)
                .withDouble(15.0)
                .withDouble(20.0)
                .withOption("Alterar arredondamento")
                .withOption("Para cima em R$ 0,50")
                .withOption("Remover insumo")
                .withOption("Limao")
                .withOption("Remover insumo")
                .withOption("Voltar")
                .withOption("Remover produto")
                .withOption("Limonada")
                .withConfirm(false)
                .withOption("Sair");

        PrecifyApp app = new PrecifyApp(repository, relatorioService, relatorio, dialog);
        app.iniciar();

        List<Produto> carregados = repository.carregar();
        assertEquals(1, carregados.size(), "Produto nao deveria ter sido removido.");
        Produto editado = carregados.get(0);

        assertEquals(CategoriaProduto.BEBIDA, editado.getCategoria(), "Categoria nao foi atualizada.");
        assertDoubleEquals(2.0, editado.getRendimento(), EPSILON, "Rendimento nao foi atualizado.");
        assertEquals("litros", editado.getUnidadeRendimento(), "Unidade de rendimento nao foi atualizada.");
        assertDoubleEquals(12.0, editado.getPercentualGastosIndiretos(), EPSILON, "Gastos nao foram atualizados.");
        assertDoubleEquals(4.0, editado.getPercentualDesperdicio(), EPSILON, "Desperdicio nao foi atualizado.");
        assertDoubleEquals(15.0, editado.getPercentualTaxaVenda(), EPSILON, "Taxa nao foi atualizada.");
        assertDoubleEquals(20.0, editado.getPercentualLucro(), EPSILON, "Lucro nao foi atualizado.");
        assertEquals(TipoArredondamento.MULTIPLO_0_50, editado.getTipoArredondamento(), "Arredondamento nao foi atualizado.");
        assertEquals(0, editado.getItensCusto().size(), "Item nao foi removido.");

        assertTrue(dialog.containsError("Opcao invalida."), "Opcao invalida do menu principal nao gerou erro.");
        assertTrue(dialog.containsMessage("Este produto nao possui insumos."), "Borda de remocao sem insumos nao foi coberta.");
        assertFalse(dialog.containsMessage("Produto removido."), "Produto nao deveria ter sido removido quando a confirmacao e negativa.");
    }

    private static Produto criarProdutoBase(String nome, TipoArredondamento arredondamento) {
        return new Produto(
                nome,
                CategoriaProduto.ALIMENTO,
                10,
                "unidades",
                10,
                5,
                12,
                30,
                0.5,
                arredondamento);
    }

    private static Produto criarProdutoComPrecoBase(double precoBase, TipoArredondamento arredondamento) {
        return new Produto(
                "Teste",
                CategoriaProduto.OUTRO,
                10,
                "unidades",
                0,
                0,
                0,
                0,
                precoBase,
                arredondamento);
    }

    private static String codificar(String valor) {
        return Base64.getUrlEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(message + " Esperado: " + expected + " Obtido: " + actual);
        }
    }

    private static void assertDoubleEquals(double expected, double actual, double epsilon, String message) {
        if (Math.abs(expected - actual) > epsilon) {
            throw new AssertionError(message + " Esperado: " + expected + " Obtido: " + actual);
        }
    }

    private static void assertContains(String actual, String expectedFragment, String message) {
        if (!normalizar(actual).contains(normalizar(expectedFragment))) {
            throw new AssertionError(message + " Conteudo atual: " + actual);
        }
    }

    private static void assertThrows(Class<? extends Throwable> expectedType, ThrowingRunnable action, String expectedMessageFragment) {
        try {
            action.run();
        } catch (Throwable erro) {
            if (!expectedType.isInstance(erro)) {
                throw new AssertionError("Tipo de excecao inesperado. Esperado: " + expectedType + " Obtido: " + erro.getClass(), erro);
            }
            if (expectedMessageFragment != null && !normalizar(erro.getMessage()).contains(normalizar(expectedMessageFragment))) {
                throw new AssertionError("Mensagem de excecao inesperada: " + erro.getMessage());
            }
            return;
        }
        throw new AssertionError("Excecao esperada nao foi lancada: " + expectedType.getName());
    }

    private static String normalizar(String texto) {
        return Normalizer.normalize(texto == null ? "" : texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);
    }

    @FunctionalInterface
    private interface ThrowingRunnable {
        void run() throws Exception;
    }

    private record TestCase(String nome, ThrowingRunnable runnable) {
        void run() throws Exception {
            runnable.run();
        }
    }

    private static final class FakeDialogService implements DialogService {
        private static final Object NULL_SENTINEL = new Object();

        private final List<Object> options = new ArrayList<>();
        private final List<Object> texts = new ArrayList<>();
        private final List<Object> doubles = new ArrayList<>();
        private final List<Boolean> confirms = new ArrayList<>();
        private final List<String> messages = new ArrayList<>();
        private final List<String> errors = new ArrayList<>();

        private int optionIndex;
        private int textIndex;
        private int doubleIndex;
        private int confirmIndex;

        FakeDialogService withOption(String value) {
            options.add(value == null ? NULL_SENTINEL : value);
            return this;
        }

        FakeDialogService withText(String value) {
            texts.add(value == null ? NULL_SENTINEL : value);
            return this;
        }

        FakeDialogService withNullText() {
            texts.add(NULL_SENTINEL);
            return this;
        }

        FakeDialogService withDouble(Double value) {
            doubles.add(value == null ? NULL_SENTINEL : value);
            return this;
        }

        FakeDialogService withConfirm(boolean value) {
            confirms.add(value);
            return this;
        }

        @Override
        public String escolherOpcao(String titulo, String mensagem, String[] opcoes) {
            return (String) next(options, optionIndex++, "opcao");
        }

        @Override
        public String solicitarTexto(String titulo, String mensagem) {
            return (String) next(texts, textIndex++, "texto");
        }

        @Override
        public Double solicitarDouble(String titulo, String mensagem, boolean permiteZero) {
            return (Double) next(doubles, doubleIndex++, "double");
        }

        @Override
        public boolean confirmar(String titulo, String mensagem) {
            if (confirmIndex >= confirms.size()) {
                throw new AssertionError("Confirmacao nao fornecida para: " + mensagem);
            }
            return confirms.get(confirmIndex++);
        }

        @Override
        public void mostrarMensagem(String titulo, String mensagem) {
            messages.add(mensagem);
        }

        @Override
        public void mostrarErro(String mensagem) {
            errors.add(mensagem);
        }

        boolean containsMessage(String fragmento) {
            return messages.stream().map(PrecifyAppTestSuite::normalizar).anyMatch(m -> m.contains(normalizar(fragmento)));
        }

        boolean containsError(String fragmento) {
            return errors.stream().map(PrecifyAppTestSuite::normalizar).anyMatch(m -> m.contains(normalizar(fragmento)));
        }

        long countMessagesContaining(String fragmento) {
            return messages.stream().map(PrecifyAppTestSuite::normalizar).filter(m -> m.contains(normalizar(fragmento))).count();
        }

        private Object next(List<Object> values, int index, String tipo) {
            if (index >= values.size()) {
                throw new AssertionError("Valor de " + tipo + " nao fornecido para o teste.");
            }
            Object value = values.get(index);
            return value == NULL_SENTINEL ? null : value;
        }
    }
}
