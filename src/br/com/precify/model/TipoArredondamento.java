package br.com.precify.model;

public enum TipoArredondamento {
    SEM_ARREDONDAMENTO("Sem arredondamento"),
    MULTIPLO_0_10("Para cima em R$ 0,10"),
    MULTIPLO_0_50("Para cima em R$ 0,50"),
    MULTIPLO_1_00("Para cima em R$ 1,00"),
    FINAL_99("Final ,99");

    private final String descricao;

    TipoArredondamento(String descricao) {
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

