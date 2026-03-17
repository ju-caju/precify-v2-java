package br.com.precify.model;

public class ItemCusto {
    private final Insumo insumo;
    private double quantidadeUsada;

    public ItemCusto(Insumo insumo, double quantidadeUsada) {
        if (insumo == null) {
            throw new IllegalArgumentException("Insumo obrigatorio.");
        }
        setQuantidadeUsada(quantidadeUsada);
        this.insumo = insumo;
    }

    public Insumo getInsumo() {
        return insumo;
    }

    public double getQuantidadeUsada() {
        return quantidadeUsada;
    }

    public void setQuantidadeUsada(double quantidadeUsada) {
        if (quantidadeUsada < 0) {
            throw new IllegalArgumentException("Quantidade usada nao pode ser negativa.");
        }
        this.quantidadeUsada = quantidadeUsada;
    }

    public double calcularCusto() {
        return insumo.calcularCusto(quantidadeUsada);
    }
}

