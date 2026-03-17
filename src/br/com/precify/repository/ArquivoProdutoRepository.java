package br.com.precify.repository;

import br.com.precify.model.CategoriaProduto;
import br.com.precify.model.Insumo;
import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;
import br.com.precify.model.TipoArredondamento;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class ArquivoProdutoRepository implements ProdutoRepository {
    private static final String PREFIXO_PRODUTO = "PRODUTO";
    private static final String PREFIXO_ITEM = "ITEM";
    private static final String PREFIXO_FIM = "FIM";

    private final Path arquivo;

    public ArquivoProdutoRepository(Path arquivo) {
        this.arquivo = arquivo;
    }

    @Override
    public List<Produto> carregar() throws IOException {
        List<Produto> produtos = new ArrayList<>();
        if (!Files.exists(arquivo)) {
            return produtos;
        }

        List<String> linhas = Files.readAllLines(arquivo, StandardCharsets.UTF_8);
        Produto produtoAtual = null;

        for (String linha : linhas) {
            if (linha.isBlank()) {
                continue;
            }

            String[] partes = linha.split("\\|");
            String tipoRegistro = partes[0];

            if (PREFIXO_PRODUTO.equals(tipoRegistro)) {
                double percentualDesperdicio = partes.length > 8 ? Double.parseDouble(partes[6]) : 0.0;
                double percentualTaxaVenda = partes.length > 8 ? Double.parseDouble(partes[7]) : 0.0;
                double percentualLucro = partes.length > 8 ? Double.parseDouble(partes[8]) : Double.parseDouble(partes[6]);
                double custoEmbalagem = partes.length > 8 ? Double.parseDouble(partes[9]) : Double.parseDouble(partes[7]);
                TipoArredondamento tipoArredondamento = partes.length > 8
                        ? TipoArredondamento.valueOf(partes[10])
                        : TipoArredondamento.SEM_ARREDONDAMENTO;
                produtoAtual = new Produto(
                        decodificar(partes[1]),
                        CategoriaProduto.valueOf(partes[2]),
                        Double.parseDouble(partes[3]),
                        decodificar(partes[4]),
                        Double.parseDouble(partes[5]),
                        percentualDesperdicio,
                        percentualTaxaVenda,
                        percentualLucro,
                        custoEmbalagem,
                        tipoArredondamento);
                produtos.add(produtoAtual);
                continue;
            }

            if (PREFIXO_ITEM.equals(tipoRegistro) && produtoAtual != null) {
                Insumo insumo = new Insumo(
                        decodificar(partes[1]),
                        decodificar(partes[2]),
                        Double.parseDouble(partes[3]),
                        Double.parseDouble(partes[4]));
                produtoAtual.adicionarItemCusto(new ItemCusto(insumo, Double.parseDouble(partes[5])));
                continue;
            }

            if (PREFIXO_FIM.equals(tipoRegistro)) {
                produtoAtual = null;
            }
        }

        return produtos;
    }

    @Override
    public void salvar(List<Produto> produtos) throws IOException {
        Files.createDirectories(arquivo.getParent());

        List<String> linhas = new ArrayList<>();
        for (Produto produto : produtos) {
            linhas.add(String.join("|",
                    PREFIXO_PRODUTO,
                    codificar(produto.getNome()),
                    produto.getCategoria().name(),
                    Double.toString(produto.getRendimento()),
                    codificar(produto.getUnidadeRendimento()),
                    Double.toString(produto.getPercentualGastosIndiretos()),
                    Double.toString(produto.getPercentualDesperdicio()),
                    Double.toString(produto.getPercentualTaxaVenda()),
                    Double.toString(produto.getPercentualLucro()),
                    Double.toString(produto.getCustoEmbalagemUnitario()),
                    produto.getTipoArredondamento().name()));

            for (ItemCusto item : produto.getItensCusto()) {
                linhas.add(String.join("|",
                        PREFIXO_ITEM,
                        codificar(item.getInsumo().getNome()),
                        codificar(item.getInsumo().getUnidadeMedida()),
                        Double.toString(item.getInsumo().getQuantidadeEmbalagem()),
                        Double.toString(item.getInsumo().getPrecoEmbalagem()),
                        Double.toString(item.getQuantidadeUsada())));
            }

            linhas.add(PREFIXO_FIM);
        }

        Files.write(arquivo, linhas, StandardCharsets.UTF_8);
    }

    private String codificar(String valor) {
        return Base64.getUrlEncoder().encodeToString(valor.getBytes(StandardCharsets.UTF_8));
    }

    private String decodificar(String valor) {
        return new String(Base64.getUrlDecoder().decode(valor), StandardCharsets.UTF_8);
    }
}
