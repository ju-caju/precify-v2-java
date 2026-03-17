package br.com.precify.model;

public enum CategoriaProduto {
    ALIMENTO("Alimento"),
    BEBIDA("Bebida"),
    SOBREMESA("Sobremesa"),
    ARTESANAL("Artesanal"),
    OUTRO("Outro");

    private final String descricao;

    CategoriaProduto(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

    @Override
    public String toString() {
        return descricao;
    }
}

