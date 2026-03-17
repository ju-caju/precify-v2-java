package br.com.precify.service;

import br.com.precify.model.Produto;

public interface CalculadoraDePreco {
    double calcularCustoMateriais(Produto produto);

    double calcularCustoEmbalagemLote(Produto produto);

    double calcularCustoTotalLote(Produto produto);

    double calcularPrecoSugeridoUnitario(Produto produto);
}

