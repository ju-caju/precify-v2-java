package br.com.precify.repository;

import br.com.precify.model.Produto;
import java.io.IOException;
import java.util.List;

public interface ProdutoRepository {
    List<Produto> carregar() throws IOException;

    void salvar(List<Produto> produtos) throws IOException;
}

