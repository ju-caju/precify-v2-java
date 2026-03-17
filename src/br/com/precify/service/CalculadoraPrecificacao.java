package br.com.precify.service;

import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;
import br.com.precify.model.TipoArredondamento;

public class CalculadoraPrecificacao implements CalculadoraDePreco {
    @Override
    public double calcularCustoMateriais(Produto produto) {
        double total = 0.0;
        for (ItemCusto item : produto.getItensCusto()) {
            total += item.calcularCusto();
        }
        return total;
    }

    @Override
    public double calcularCustoEmbalagemLote(Produto produto) {
        return produto.getCustoEmbalagemUnitario() * produto.getRendimento();
    }

    @Override
    public double calcularSubtotalBase(Produto produto) {
        return calcularCustoMateriais(produto) + calcularCustoEmbalagemLote(produto);
    }

    @Override
    public double calcularCustoTotalLoteSemArredondamento(Produto produto) {
        double subtotal = calcularSubtotalBase(produto);
        double fatorDesperdicio = 1 + (produto.getPercentualDesperdicio() / 100.0);
        double fatorGastos = 1 + (produto.getPercentualGastosIndiretos() / 100.0);
        double fatorTaxaVenda = 1 + (produto.getPercentualTaxaVenda() / 100.0);
        double fatorLucro = 1 + (produto.getPercentualLucro() / 100.0);
        return subtotal * fatorDesperdicio * fatorGastos * fatorTaxaVenda * fatorLucro;
    }

    @Override
    public double calcularPrecoSugeridoUnitario(Produto produto) {
        if (produto.getRendimento() <= 0) {
            return 0.0;
        }
        double precoBase = calcularCustoTotalLoteSemArredondamento(produto) / produto.getRendimento();
        return aplicarArredondamento(precoBase, produto.getTipoArredondamento());
    }

    private double aplicarArredondamento(double valor, TipoArredondamento tipo) {
        return switch (tipo) {
            case SEM_ARREDONDAMENTO -> valor;
            case MULTIPLO_0_10 -> arredondarParaCima(valor, 0.10);
            case MULTIPLO_0_50 -> arredondarParaCima(valor, 0.50);
            case MULTIPLO_1_00 -> arredondarParaCima(valor, 1.00);
            case FINAL_99 -> arredondarFinal99(valor);
        };
    }

    private double arredondarParaCima(double valor, double multiplo) {
        return Math.ceil(valor / multiplo) * multiplo;
    }

    private double arredondarFinal99(double valor) {
        double base = Math.floor(valor);
        double candidato = base + 0.99;
        if (candidato >= valor) {
            return candidato;
        }
        return base + 1.99;
    }
}
