package br.com.ebac;

import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.domain.Cliente;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO em memória para testar o service sem depender do banco.
 */
public class ClienteDAOMock implements IClienteDAO {

    private final Map<Long, Cliente> clientes = new LinkedHashMap<>();
    private long proximoId = 1;

    @Override
    public Integer cadastrar(Cliente cliente) {
        cliente.setId(proximoId++);
        clientes.put(cliente.getId(), cliente);
        return 1;
    }

    @Override
    public Cliente buscarPorCpf(String cpf) {
        return clientes.values().stream()
                .filter(c -> c.getCpf().equals(cpf))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<Cliente> buscarTodos() {
        return new ArrayList<>(clientes.values());
    }

    @Override
    public Integer atualizar(Cliente cliente) {
        if (!clientes.containsKey(cliente.getId())) {
            return 0;
        }
        clientes.put(cliente.getId(), cliente);
        return 1;
    }

    @Override
    public Integer excluir(Cliente cliente) {
        return clientes.remove(cliente.getId()) != null ? 1 : 0;
    }
}
