package br.com.ebac.dao;

import br.com.ebac.domain.Cliente;

import java.sql.SQLException;
import java.util.List;

public interface IClienteDAO {

    Integer cadastrar(Cliente cliente) throws SQLException;

    Cliente buscarPorCpf(String cpf) throws SQLException;

    List<Cliente> buscarTodos() throws SQLException;

    Integer atualizar(Cliente cliente) throws SQLException;

    Integer excluir(Cliente cliente) throws SQLException;
}
