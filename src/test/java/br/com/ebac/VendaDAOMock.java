package br.com.ebac;

import br.com.ebac.dao.IVendaDAO;
import br.com.ebac.domain.Venda;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO em memória para testar o service sem depender do banco.
 */
public class VendaDAOMock implements IVendaDAO {

    private final Map<Long, Venda> vendas = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Integer cadastrar(Venda venda) {
        venda.setId(proximoId++);
        vendas.put(venda.getId(), venda);
        return 1;
    }

    @Override
    public Venda buscarPorCodigo(String codigo) {
        return vendas.values().stream()
                .filter(v -> v.getCodigo().equals(codigo))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Venda> buscarTodos() {
        return new ArrayList<>(vendas.values());
    }

    @Override
    public Integer atualizar(Venda venda) {
        if (!vendas.containsKey(venda.getId())) {
            return 0;
        }
        vendas.put(venda.getId(), venda);
        return 1;
    }

    @Override
    public Integer excluir(Venda venda) {
        return vendas.remove(venda.getId()) != null ? 1 : 0;
    }
}
