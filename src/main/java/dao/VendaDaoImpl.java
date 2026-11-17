package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import factory.ConnectionFactory;
import model.Cliente;
import model.Produto;
import model.ProdutoQuantidade;
import model.Venda;

/**
 * Grava a venda em TB_VENDA e cada item (ProdutoQuantidade) em TB_PRODUTO_QUANTIDADE.
 * Venda e itens são gravados na mesma transação: ou entra tudo, ou nada.
 */
public class VendaDaoImpl implements VendaDAO {

    static final String SQL_INSERT_VENDA =
            "INSERT INTO tb_venda (codigo, id_cliente_fk, valor_total, data_venda, status_venda) VALUES (?, ?, ?, ?, ?)";

    static final String SQL_UPDATE_VENDA =
            "UPDATE tb_venda SET codigo = ?, id_cliente_fk = ?, valor_total = ?, data_venda = ?, status_venda = ? "
                    + "WHERE id = ?";

    static final String SQL_INSERT_ITEM =
            "INSERT INTO tb_produto_quantidade (id_venda_fk, id_produto_fk, quantidade, valor_total) VALUES (?, ?, ?, ?)";

    static final String SQL_DELETE_ITENS = "DELETE FROM tb_produto_quantidade WHERE id_venda_fk = ?";

    static final String SQL_DELETE_VENDA = "DELETE FROM tb_venda WHERE id = ?";

    static final String SQL_SELECT_VENDA =
            "SELECT v.id, v.codigo, v.valor_total, v.data_venda, v.status_venda, "
                    + "c.id AS c_id, c.nome AS c_nome, c.cpf AS c_cpf, c.tel AS c_tel, c.endereco AS c_endereco, "
                    + "c.numero AS c_numero, c.cidade AS c_cidade, c.estado AS c_estado, c.email AS c_email, "
                    + "c.qtd_compras AS c_qtd_compras, c.data_nascimento AS c_data_nascimento "
                    + "FROM tb_venda v JOIN tb_cliente c ON c.id = v.id_cliente_fk";

    static final String SQL_SELECT_ITENS =
            "SELECT pq.id, pq.quantidade, pq.valor_total, "
                    + "p.id AS p_id, p.codigo AS p_codigo, p.nome AS p_nome, p.descricao AS p_descricao, "
                    + "p.valor AS p_valor, p.valor_compra AS p_valor_compra, p.disponivel AS p_disponivel, "
                    + "p.marca AS p_marca "
                    + "FROM tb_produto_quantidade pq JOIN tb_produto p ON p.id = pq.id_produto_fk "
                    + "WHERE pq.id_venda_fk = ?";

    @Override
    public Integer cadastrar(Venda venda) throws Exception {
        preencherPadroes(venda);
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            stm = conn.prepareStatement(SQL_INSERT_VENDA, Statement.RETURN_GENERATED_KEYS);
            adicionarParametrosVenda(stm, venda);
            stm.executeUpdate();

            Integer id = null;
            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    id = rs.getInt(1);
                    venda.setId(id.longValue());
                }
            }

            inserirItens(conn, venda);
            conn.commit();
            return id;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /** Venda sem data ou status recebe a data atual e o status INICIADA. */
    private void preencherPadroes(Venda venda) {
        if (venda.getDataVenda() == null) {
            venda.setDataVenda(Instant.now());
        }
        if (venda.getStatus() == null) {
            venda.setStatus(Venda.Status.INICIADA);
        }
    }

    /** Parâmetros 1 a 5, comuns ao INSERT e ao UPDATE da venda. */
    private void adicionarParametrosVenda(PreparedStatement stm, Venda venda) throws Exception {
        stm.setString(1, venda.getCodigo());
        stm.setLong(2, venda.getCliente().getId());
        stm.setBigDecimal(3, venda.getValorTotal());
        stm.setTimestamp(4, Timestamp.from(venda.getDataVenda()));
        stm.setString(5, venda.getStatus().name());
    }

    private void inserirItens(Connection conn, Venda venda) throws Exception {
        try (PreparedStatement stm = conn.prepareStatement(SQL_INSERT_ITEM, Statement.RETURN_GENERATED_KEYS)) {
            for (ProdutoQuantidade item : venda.getProdutos()) {
                stm.setLong(1, venda.getId());
                stm.setLong(2, item.getProduto().getId());
                stm.setInt(3, item.getQuantidade());
                stm.setBigDecimal(4, item.getValorTotal());
                stm.executeUpdate();

                try (ResultSet rs = stm.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setId(rs.getLong(1));
                    }
                }
            }
        }
    }

    /** Atualiza a venda e substitui todos os itens pelos que estão no objeto. */
    @Override
    public Integer atualizar(Venda venda) throws Exception {
        preencherPadroes(venda);
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            conn.setAutoCommit(false);

            stm = conn.prepareStatement(SQL_UPDATE_VENDA);
            adicionarParametrosVenda(stm, venda);
            stm.setLong(6, venda.getId());
            Integer linhas = stm.executeUpdate();

            try (PreparedStatement delete = conn.prepareStatement(SQL_DELETE_ITENS)) {
                delete.setLong(1, venda.getId());
                delete.executeUpdate();
            }
            inserirItens(conn, venda);

            conn.commit();
            return linhas;
        } catch (Exception e) {
            if (conn != null) {
                conn.rollback();
            }
            throw e;
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
    public Venda buscaVenda(String codigo) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(SQL_SELECT_VENDA + " WHERE v.codigo = ?");
            stm.setString(1, codigo);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    Venda venda = montaVenda(rs);
                    venda.setProdutos(buscaItens(conn, venda.getId()));
                    return venda;
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
    public List<Venda> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Venda> vendas = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(SQL_SELECT_VENDA);

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    vendas.add(montaVenda(rs));
                }
            }
            for (Venda venda : vendas) {
                venda.setProdutos(buscaItens(conn, venda.getId()));
            }
            return vendas;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    /** Os itens da venda são removidos pelo banco (ON DELETE CASCADE). */
    @Override
    public Integer excluir(Venda venda) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(SQL_DELETE_VENDA);
            stm.setLong(1, venda.getId());
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

    private Set<ProdutoQuantidade> buscaItens(Connection conn, Long idVenda) throws Exception {
        Set<ProdutoQuantidade> itens = new HashSet<>();
        try (PreparedStatement stm = conn.prepareStatement(SQL_SELECT_ITENS)) {
            stm.setLong(1, idVenda);
            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    itens.add(montaItem(rs));
                }
            }
        }
        return itens;
    }

    private Venda montaVenda(ResultSet rs) throws Exception {
        Venda venda = new Venda();
        venda.setId(rs.getLong("id"));
        venda.setCodigo(rs.getString("codigo"));
        venda.setValorTotal(rs.getBigDecimal("valor_total"));
        venda.setDataVenda(rs.getTimestamp("data_venda").toInstant());
        venda.setStatus(Venda.Status.getByName(rs.getString("status_venda")));
        venda.setCliente(montaCliente(rs));
        return venda;
    }

    private Cliente montaCliente(ResultSet rs) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("c_id"));
        cliente.setNome(rs.getString("c_nome"));
        cliente.setCpf(rs.getLong("c_cpf"));
        cliente.setTel(rs.getObject("c_tel", Long.class));
        cliente.setEnd(rs.getString("c_endereco"));
        cliente.setNumero(rs.getObject("c_numero", Integer.class));
        cliente.setCidade(rs.getString("c_cidade"));
        cliente.setEstado(rs.getString("c_estado"));
        cliente.setEmail(rs.getString("c_email"));
        cliente.setQtdCompras(rs.getInt("c_qtd_compras"));
        cliente.setDataNascimento(rs.getObject("c_data_nascimento", LocalDate.class));
        return cliente;
    }

    private ProdutoQuantidade montaItem(ResultSet rs) throws Exception {
        Produto produto = new Produto();
        produto.setId(rs.getLong("p_id"));
        produto.setCodigo(rs.getString("p_codigo"));
        produto.setNome(rs.getString("p_nome"));
        produto.setDescricao(rs.getString("p_descricao"));
        produto.setValor(rs.getBigDecimal("p_valor"));
        produto.setValorCompra(rs.getBigDecimal("p_valor_compra"));
        produto.setDisponivel(rs.getBoolean("p_disponivel"));
        produto.setMarca(rs.getString("p_marca"));

        ProdutoQuantidade item = new ProdutoQuantidade();
        item.setId(rs.getLong("id"));
        item.setProduto(produto);
        item.setQuantidade(rs.getInt("quantidade"));
        item.setValorTotal(rs.getBigDecimal("valor_total"));
        return item;
    }
}
