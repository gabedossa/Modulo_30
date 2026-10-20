package br.com.ebac.dao;

import br.com.ebac.dao.jdbc.ConnectionFactory;
import br.com.ebac.domain.Produto;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO implements IProdutoDAO {

    @Override
    public Integer cadastrar(Produto produto) throws SQLException {
        String sql = "INSERT INTO tb_produto (codigo, nome, descricao, valor) VALUES (?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherParametros(stm, produto);
            int linhas = stm.executeUpdate();
            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    produto.setId(rs.getLong(1));
                }
            }
            return linhas;
        }
    }

    @Override
    public Produto buscarPorCodigo(String codigo) throws SQLException {
        String sql = "SELECT * FROM tb_produto WHERE codigo = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setString(1, codigo);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? converter(rs) : null;
            }
        }
    }

    @Override
    public List<Produto> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM tb_produto ORDER BY id";
        List<Produto> produtos = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                produtos.add(converter(rs));
            }
        }
        return produtos;
    }

    @Override
    public Integer atualizar(Produto produto) throws SQLException {
        String sql = "UPDATE tb_produto SET codigo = ?, nome = ?, descricao = ?, valor = ? WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            preencherParametros(stm, produto);
            stm.setLong(5, produto.getId());
            return stm.executeUpdate();
        }
    }

    @Override
    public Integer excluir(Produto produto) throws SQLException {
        String sql = "DELETE FROM tb_produto WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, produto.getId());
            return stm.executeUpdate();
        }
    }

    private void preencherParametros(PreparedStatement stm, Produto produto) throws SQLException {
        stm.setString(1, produto.getCodigo());
        stm.setString(2, produto.getNome());
        stm.setString(3, produto.getDescricao());
        stm.setBigDecimal(4, produto.getValor());
    }

    static Produto converter(ResultSet rs) throws SQLException {
        Produto produto = new Produto();
        produto.setId(rs.getLong("id"));
        produto.setCodigo(rs.getString("codigo"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setValor(rs.getBigDecimal("valor"));
        return produto;
    }
}
