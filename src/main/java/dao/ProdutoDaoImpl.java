package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Produto;

public class ProdutoDaoImpl implements ProdutoDAO {

    private static final String COLUNAS = "id, codigo, nome, descricao, valor, valor_compra, disponivel, marca";

    @Override
    public Integer cadastrar(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametros(stm, produto);
            stm.executeUpdate();

            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlInsert() {
        return "INSERT INTO tb_produto (codigo, nome, descricao, valor, valor_compra, disponivel, marca) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    /** Parâmetros 1 a 7, comuns ao INSERT e ao UPDATE. */
    private void adicionarParametros(PreparedStatement stm, Produto produto) throws Exception {
        stm.setString(1, produto.getCodigo());
        stm.setString(2, produto.getNome());
        stm.setString(3, produto.getDescricao());
        stm.setBigDecimal(4, produto.getValor());
        stm.setBigDecimal(5, produto.getValorCompra());
        stm.setBoolean(6, produto.getDisponivel());
        stm.setString(7, produto.getMarca());
    }

    @Override
    public Integer atualizar(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametros(stm, produto);
            stm.setLong(8, produto.getId());
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlUpdate() {
        return "UPDATE tb_produto SET codigo = ?, nome = ?, descricao = ?, valor = ?, valor_compra = ?, "
                + "disponivel = ?, marca = ? WHERE id = ?";
    }

    @Override
    public Produto buscaProduto(String codigo) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT " + COLUNAS + " FROM tb_produto WHERE codigo = ?");
            stm.setString(1, codigo);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return montaProduto(rs);
                }
            }
            return null;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public List<Produto> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Produto> produtos = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT " + COLUNAS + " FROM tb_produto");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    produtos.add(montaProduto(rs));
                }
            }
            return produtos;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public Integer excluir(Produto produto) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM tb_produto WHERE id = ?");
            stm.setLong(1, produto.getId());
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private Produto montaProduto(ResultSet rs) throws Exception {
        Produto produto = new Produto();
        produto.setId(rs.getLong("id"));
        produto.setCodigo(rs.getString("codigo"));
        produto.setNome(rs.getString("nome"));
        produto.setDescricao(rs.getString("descricao"));
        produto.setValor(rs.getBigDecimal("valor"));
        produto.setValorCompra(rs.getBigDecimal("valor_compra"));
        produto.setDisponivel(rs.getBoolean("disponivel"));
        produto.setMarca(rs.getString("marca"));
        return produto;
    }
}
