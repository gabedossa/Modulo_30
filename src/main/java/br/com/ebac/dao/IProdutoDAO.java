package br.com.ebac.dao;

import br.com.ebac.domain.Produto;

import java.sql.SQLException;
import java.util.List;

public interface IProdutoDAO {

    Integer cadastrar(Produto produto) throws SQLException;

    Produto buscarPorCodigo(String codigo) throws SQLException;

    List<Produto> buscarTodos() throws SQLException;

    Integer atualizar(Produto produto) throws SQLException;

    Integer excluir(Produto produto) throws SQLException;
}
