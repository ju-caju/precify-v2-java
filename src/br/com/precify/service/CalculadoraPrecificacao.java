package br.com.precify.service;

import br.com.precify.model.ItemCusto;
import br.com.precify.model.Produto;

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
    public double calcularCustoTotalLote(Produto produto) {
        double subtotal = calcularCustoMateriais(produto) + calcularCustoEmbalagemLote(produto);
        double fatorGastos = 1 + (produto.getPercentualGastosIndiretos() / 100.0);
        double fatorLucro = 1 + (produto.getPercentualLucro() / 100.0);
        return subtotal * fatorGastos * fatorLucro;
    }

    @Override
    public double calcularPrecoSugeridoUnitario(Produto produto) {
        if (produto.getRendimento() <= 0) {
            return 0.0;
        }
        return calcularCustoTotalLote(produto) / produto.getRendimento();
    }
}

