package br.com.ebac.service;

import br.com.ebac.domain.Cliente;

import java.sql.SQLException;
import java.util.List;

public interface IClienteService {

    Cliente cadastrar(Cliente cliente) throws SQLException;

    Cliente buscarPorCpf(String cpf) throws SQLException;

    List<Cliente> buscarTodos() throws SQLException;

    Cliente atualizar(Cliente cliente) throws SQLException;

    void excluir(String cpf) throws SQLException;
}
