package br.com.precify.model;

public class Insumo {
    private final String nome;
    private final String unidadeMedida;
    private final double quantidadeEmbalagem;
    private final double precoEmbalagem;

    public Insumo(String nome, String unidadeMedida, double quantidadeEmbalagem, double precoEmbalagem) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do insumo nao pode ser vazio.");
        }
        if (unidadeMedida == null || unidadeMedida.isBlank()) {
            throw new IllegalArgumentException("Unidade do insumo nao pode ser vazia.");
        }
        if (quantidadeEmbalagem <= 0) {
            throw new IllegalArgumentException("Quantidade da embalagem deve ser maior que zero.");
        }
        if (precoEmbalagem < 0) {
            throw new IllegalArgumentException("Preco da embalagem nao pode ser negativo.");
        }

        this.nome = nome.trim();
        this.unidadeMedida = unidadeMedida.trim();
        this.quantidadeEmbalagem = quantidadeEmbalagem;
        this.precoEmbalagem = precoEmbalagem;
    }

    public String getNome() {
        return nome;
    }

    public String getUnidadeMedida() {
        return unidadeMedida;
    }

    public double getQuantidadeEmbalagem() {
        return quantidadeEmbalagem;
    }

    public double getPrecoEmbalagem() {
        return precoEmbalagem;
    }

    public double calcularCusto(double quantidadeUsada) {
        if (quantidadeUsada < 0) {
            throw new IllegalArgumentException("Quantidade usada nao pode ser negativa.");
        }
        return (precoEmbalagem / quantidadeEmbalagem) * quantidadeUsada;
    }
}

