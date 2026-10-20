package br.com.ebac.dao;

import br.com.ebac.dao.generic.GenericDAO;
import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.ProdutoQuantidade;
import br.com.ebac.domain.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class VendaDAO extends GenericDAO<Venda, String> implements IVendaDAO {

    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final ProdutoDAO produtoDAO = new ProdutoDAO();

    @Override
    public Class<Venda> getTipoClasse() {
        return Venda.class;
    }

    @Override
    protected void depoisDeCadastrar(Connection connection, Venda venda) throws SQLException {
        inserirItens(connection, venda);
    }

    @Override
    protected void depoisDeAtualizar(Connection connection, Venda venda) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement(
                "DELETE FROM tb_produto_quantidade WHERE venda_id = ?")) {
            stm.setLong(1, venda.getId());
            stm.executeUpdate();
        }
        inserirItens(connection, venda);
    }

    @Override
    public Venda converter(Connection connection, ResultSet rs) throws SQLException {
        // os campos simples vêm das annotations; cliente e itens são buscados à parte
        Venda venda = super.converter(connection, rs);
        venda.setCliente(buscarCliente(connection, rs.getLong("cliente_id")));
        venda.setProdutos(buscarItens(connection, venda.getId()));
        return venda;
    }

    private void inserirItens(Connection connection, Venda venda) throws SQLException {
        String sql = "INSERT INTO tb_produto_quantidade (venda_id, produto_id, quantidade, valor_total) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            for (ProdutoQuantidade item : venda.getProdutos()) {
                stm.setLong(1, venda.getId());
                stm.setLong(2, item.getProduto().getId());
                stm.setInt(3, item.getQuantidade());
                stm.setBigDecimal(4, item.getValorTotal());
                stm.addBatch();
            }
            stm.executeBatch();
        }
    }

    private Cliente buscarCliente(Connection connection, Long clienteId) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement("SELECT * FROM tb_cliente WHERE id = ?")) {
            stm.setLong(1, clienteId);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? clienteDAO.converter(connection, rs) : null;
            }
        }
    }

    private List<ProdutoQuantidade> buscarItens(Connection connection, Long vendaId) throws SQLException {
        String sql = "SELECT pq.quantidade, pq.valor_total AS item_valor_total, p.* "
                + "FROM tb_produto_quantidade pq JOIN tb_produto p ON p.id = pq.produto_id "
                + "WHERE pq.venda_id = ? ORDER BY pq.id";
        List<ProdutoQuantidade> itens = new ArrayList<>();
        try (PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, vendaId);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    ProdutoQuantidade item = new ProdutoQuantidade(produtoDAO.converter(connection, rs));
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setValorTotal(rs.getBigDecimal("item_valor_total"));
                    itens.add(item);
                }
            }
        }
        return itens;
    }
}
