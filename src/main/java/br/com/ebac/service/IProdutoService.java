package br.com.ebac.service;

import br.com.ebac.domain.Produto;

import java.sql.SQLException;
import java.util.List;

public interface IProdutoService {

    Produto cadastrar(Produto produto) throws SQLException;

    Produto buscarPorCodigo(String codigo) throws SQLException;

    List<Produto> buscarTodos() throws SQLException;

    Produto atualizar(Produto produto) throws SQLException;

    void excluir(String codigo) throws SQLException;
}
