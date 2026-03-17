package br.com.precify.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Produto {
    private String nome;
    private CategoriaProduto categoria;
    private double rendimento;
    private String unidadeRendimento;
    private double percentualGastosIndiretos;
    private double percentualLucro;
    private double custoEmbalagemUnitario;
    private final List<ItemCusto> itensCusto;

    public Produto(
            String nome,
            CategoriaProduto categoria,
            double rendimento,
            String unidadeRendimento,
            double percentualGastosIndiretos,
            double percentualLucro,
            double custoEmbalagemUnitario) {
        this.itensCusto = new ArrayList<>();
        setNome(nome);
        setCategoria(categoria);
        setRendimento(rendimento);
        setUnidadeRendimento(unidadeRendimento);
        setPercentualGastosIndiretos(percentualGastosIndiretos);
        setPercentualLucro(percentualLucro);
        setCustoEmbalagemUnitario(custoEmbalagemUnitario);
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome do produto nao pode ser vazio.");
        }
        this.nome = nome.trim();
    }

    public CategoriaProduto getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaProduto categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria obrigatoria.");
        }
        this.categoria = categoria;
    }

    public double getRendimento() {
        return rendimento;
    }

    public void setRendimento(double rendimento) {
        if (rendimento <= 0) {
            throw new IllegalArgumentException("Rendimento deve ser maior que zero.");
        }
        this.rendimento = rendimento;
    }

    public String getUnidadeRendimento() {
        return unidadeRendimento;
    }

    public void setUnidadeRendimento(String unidadeRendimento) {
        if (unidadeRendimento == null || unidadeRendimento.isBlank()) {
            throw new IllegalArgumentException("Unidade de rendimento obrigatoria.");
        }
        this.unidadeRendimento = unidadeRendimento.trim();
    }

    public double getPercentualGastosIndiretos() {
        return percentualGastosIndiretos;
    }

    public void setPercentualGastosIndiretos(double percentualGastosIndiretos) {
        if (percentualGastosIndiretos < 0) {
            throw new IllegalArgumentException("Percentual de gastos nao pode ser negativo.");
        }
        this.percentualGastosIndiretos = percentualGastosIndiretos;
    }

    public double getPercentualLucro() {
        return percentualLucro;
    }

    public void setPercentualLucro(double percentualLucro) {
        if (percentualLucro < 0) {
            throw new IllegalArgumentException("Percentual de lucro nao pode ser negativo.");
        }
        this.percentualLucro = percentualLucro;
    }

    public double getCustoEmbalagemUnitario() {
        return custoEmbalagemUnitario;
    }

    public void setCustoEmbalagemUnitario(double custoEmbalagemUnitario) {
        if (custoEmbalagemUnitario < 0) {
            throw new IllegalArgumentException("Custo da embalagem nao pode ser negativo.");
        }
        this.custoEmbalagemUnitario = custoEmbalagemUnitario;
    }

    public List<ItemCusto> getItensCusto() {
        return Collections.unmodifiableList(itensCusto);
    }

    public void adicionarItemCusto(ItemCusto itemCusto) {
        if (itemCusto == null) {
            throw new IllegalArgumentException("Item de custo obrigatorio.");
        }
        itensCusto.add(itemCusto);
    }

    public void removerItemCusto(int indice) {
        itensCusto.remove(indice);
    }

    public boolean possuiItens() {
        return !itensCusto.isEmpty();
    }
}

