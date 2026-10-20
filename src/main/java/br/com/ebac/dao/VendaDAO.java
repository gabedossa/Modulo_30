package br.com.ebac.dao;

import br.com.ebac.dao.jdbc.ConnectionFactory;
import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.ProdutoQuantidade;
import br.com.ebac.domain.Venda;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Grava a venda em tb_venda e os itens em tb_produto_quantidade, sempre na mesma transação.
 */
public class VendaDAO implements IVendaDAO {

    @Override
    public Integer cadastrar(Venda venda) throws SQLException {
        String sql = "INSERT INTO tb_venda (codigo, cliente_id, valor_total, data_venda, status) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int linhas;
                try (PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    preencherParametros(stm, venda);
                    linhas = stm.executeUpdate();
                    try (ResultSet rs = stm.getGeneratedKeys()) {
                        if (rs.next()) {
                            venda.setId(rs.getLong(1));
                        }
                    }
                }
                inserirItens(connection, venda);
                connection.commit();
                return linhas;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public Venda buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM tb_venda WHERE codigo = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setString(1, codigo);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? converter(connection, rs) : null;
            }
        }
    }

    @Override
    public List<Venda> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM tb_venda ORDER BY id";
        List<Venda> vendas = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                vendas.add(converter(connection, rs));
            }
        }
        return vendas;
    }

    @Override
    public Integer atualizar(Venda venda) throws SQLException {
        String sql = "UPDATE tb_venda SET codigo = ?, cliente_id = ?, valor_total = ?, data_venda = ?, status = ? "
                + "WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int linhas;
                try (PreparedStatement stm = connection.prepareStatement(sql)) {
                    preencherParametros(stm, venda);
                    stm.setLong(6, venda.getId());
                    linhas = stm.executeUpdate();
                }
                try (PreparedStatement stm = connection.prepareStatement(
                        "DELETE FROM tb_produto_quantidade WHERE venda_id = ?")) {
                    stm.setLong(1, venda.getId());
                    stm.executeUpdate();
                }
                inserirItens(connection, venda);
                connection.commit();
                return linhas;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    @Override
    public Integer excluir(Venda venda) throws SQLException {
        // os itens são apagados pelo ON DELETE CASCADE de tb_produto_quantidade
        String sql = "DELETE FROM tb_venda WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, venda.getId());
            return stm.executeUpdate();
        }
    }

    private void preencherParametros(PreparedStatement stm, Venda venda) throws SQLException {
        stm.setString(1, venda.getCodigo());
        stm.setLong(2, venda.getCliente().getId());
        stm.setBigDecimal(3, venda.getValorTotal());
        stm.setTimestamp(4, Timestamp.valueOf(venda.getDataVenda()));
        stm.setString(5, venda.getStatus().name());
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

    private Venda converter(Connection connection, ResultSet rs) throws SQLException {
        Venda venda = new Venda();
        venda.setId(rs.getLong("id"));
        venda.setCodigo(rs.getString("codigo"));
        venda.setValorTotal(rs.getBigDecimal("valor_total"));
        venda.setDataVenda(rs.getTimestamp("data_venda").toLocalDateTime());
        venda.setStatus(Venda.Status.valueOf(rs.getString("status")));
        venda.setCliente(buscarCliente(connection, rs.getLong("cliente_id")));
        venda.setProdutos(buscarItens(connection, venda.getId()));
        return venda;
    }

    private Cliente buscarCliente(Connection connection, Long clienteId) throws SQLException {
        try (PreparedStatement stm = connection.prepareStatement("SELECT * FROM tb_cliente WHERE id = ?")) {
            stm.setLong(1, clienteId);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? ClienteDAO.converter(rs) : null;
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
                    ProdutoQuantidade item = new ProdutoQuantidade(ProdutoDAO.converter(rs));
                    item.setQuantidade(rs.getInt("quantidade"));
                    item.setValorTotal(rs.getBigDecimal("item_valor_total"));
                    itens.add(item);
                }
            }
        }
        return itens;
    }
}
