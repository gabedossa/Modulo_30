package br.com.ebac.dao;

import br.com.ebac.dao.generic.GenericDAO;
import br.com.ebac.domain.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ProdutoDAO extends GenericDAO<Produto, String> implements IProdutoDAO {

    @Override
    protected String getTabela() {
        return "tb_produto";
    }

    @Override
    protected String getColunaChave() {
        return "codigo";
    }

    @Override
    protected String[] getColunas() {
        return new String[]{"codigo", "nome", "descricao", "valor"};
    }

    @Override
    protected void preencherParametros(PreparedStatement stm, Produto produto) throws SQLException {
        stm.setString(1, produto.getCodigo());
        stm.setString(2, produto.getNome());
        stm.setString(3, produto.getDescricao());
        stm.setBigDecimal(4, produto.getValor());
    }

    @Override
    protected Produto converter(Connection connection, ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getLong("id"));
        produto.setCodigo(rs.getString("codigo"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setValor(rs.getBigDecimal("valor"));
        return produto;
    }
}
