package br.com.ebac;

import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.domain.Produto;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO em memória para testar os services sem depender do banco.
 */
public class ProdutoDAOMock implements IProdutoDAO {

    private final Map<Long, Produto> produtos = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Integer cadastrar(Produto produto) {
        produto.setId(proximoId++);
        produtos.put(produto.getId(), produto);
        return 1;
    }

    @Override
    public Produto buscarPorCodigo(String codigo) {
        return produtos.values().stream()
                .filter(p -> p.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Produto> buscarTodos() {
        return new ArrayList<>(produtos.values());
    }

    @Override
    public Integer atualizar(Produto produto) {
        if (!produtos.containsKey(produto.getId())) {
            return 0;
        }
        produtos.put(produto.getId(), produto);
        return 1;
    }

    @Override
    public Integer excluir(Produto produto) {
        return produtos.remove(produto.getId()) != null ? 1 : 0;
    }
}
