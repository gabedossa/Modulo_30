package br.com.ebac.service;

import br.com.ebac.domain.Venda;

import java.sql.SQLException;
import java.util.List;

public interface IVendaService {

    Venda cadastrar(Venda venda) throws SQLException;

    Venda buscarPorCodigo(String codigo) throws SQLException;

    List<Venda> buscarTodos() throws SQLException;

    Venda adicionarProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException;

    Venda removerProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException;

    Venda finalizar(String codigoVenda) throws SQLException;

    Venda cancelar(String codigoVenda) throws SQLException;

    void excluir(String codigoVenda) throws SQLException;
}
