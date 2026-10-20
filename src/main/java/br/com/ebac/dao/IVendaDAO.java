package br.com.ebac.dao;

import br.com.ebac.domain.Venda;

import java.sql.SQLException;
import java.util.List;

public interface IVendaDAO {

    Integer cadastrar(Venda venda) throws SQLException;

    Venda buscarPorCodigo(String codigo) throws SQLException;

    List<Venda> buscarTodos() throws SQLException;

    Integer atualizar(Venda venda) throws SQLException;

    Integer excluir(Venda venda) throws SQLException;
}
