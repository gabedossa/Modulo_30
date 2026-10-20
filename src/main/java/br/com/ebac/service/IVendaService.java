package br.com.ebac.service;

import br.com.ebac.domain.Venda;
import br.com.ebac.service.generic.IGenericService;

import java.sql.SQLException;

public interface IVendaService extends IGenericService<Venda, String> {

    Venda adicionarProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException;

    Venda removerProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException;

    Venda finalizar(String codigoVenda) throws SQLException;

    Venda cancelar(String codigoVenda) throws SQLException;
}
