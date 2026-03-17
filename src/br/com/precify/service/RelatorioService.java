package br.com.precify.service;

import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class RelatorioService {
    private final CalculadoraDePreco calculadoraDePreco;
    private final NumberFormat moeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public RelatorioService(CalculadoraDePreco calculadoraDePreco) {
        this.calculadoraDePreco = calculadoraDePreco;
    }

    public void gerar(Path arquivo, List<Produto> produtos) throws IOException {
        Files.createDirectories(arquivo.getParent());

        StringBuilder conteudo = new StringBuilder();
        conteudo.append("========================================\n");
        conteudo.append(" Relatorio de Precificacao - Precify 2.0\n");
        conteudo.append("========================================\n\n");

        if (produtos.isEmpty()) {
            conteudo.append("Nenhum produto cadastrado.\n");
        }

        for (Produto produto : produtos) {
            conteudo.append("Produto: ").append(produto.getNome()).append('\n');
            conteudo.append("Categoria: ").append(produto.getCategoria().getDescricao()).append('\n');
            conteudo.append("Rendimento: ").append(produto.getRendimento()).append(' ')
                    .append(produto.getUnidadeRendimento()).append('\n');
            conteudo.append("Desperdicio: ").append(produto.getPercentualDesperdicio()).append("%\n");
            conteudo.append("Gastos indiretos: ").append(produto.getPercentualGastosIndiretos()).append("%\n");
            conteudo.append("Taxa de venda/delivery: ").append(produto.getPercentualTaxaVenda()).append("%\n");
            conteudo.append("Lucro: ").append(produto.getPercentualLucro()).append("%\n");
            conteudo.append("Embalagem por unidade: ").append(moeda.format(produto.getCustoEmbalagemUnitario())).append('\n');
            conteudo.append("Arredondamento: ").append(produto.getTipoArredondamento().getDescricao()).append('\n');
            conteudo.append("Subtotal base do lote: ")
                    .append(moeda.format(calculadoraDePreco.calcularSubtotalBase(produto))).append('\n');
            conteudo.append("Custo total sem arredondamento: ")
                    .append(moeda.format(calculadoraDePreco.calcularCustoTotalLoteSemArredondamento(produto))).append('\n');
            conteudo.append("Preco sugerido por unidade: ")
                    .append(moeda.format(calculadoraDePreco.calcularPrecoSugeridoUnitario(produto)))
                    .append('\n');
            conteudo.append("Insumos:\n");

            if (!produto.possuiItens()) {
                conteudo.append(" - Nenhum insumo cadastrado.\n");
            } else {
                for (ItemCusto item : produto.getItensCusto()) {
                    conteudo.append(" - ")
                            .append(item.getInsumo().getNome())
                            .append(" | usado: ").append(item.getQuantidadeUsada()).append(' ')
                            .append(item.getInsumo().getUnidadeMedida())
                            .append(" | custo: ").append(moeda.format(item.calcularCusto()))
                            .append('\n');
                }
            }

            conteudo.append('\n');
        }

        Files.writeString(arquivo, conteudo.toString(), StandardCharsets.UTF_8);
    }
}
