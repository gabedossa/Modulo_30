package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.Cliente;
import model.Produto;
import model.ProdutoQuantidade;
import model.Venda;

class VendaDaoImplUnitTest {

    private static final Instant DATA = Instant.parse("2026-11-17T12:00:00Z");

    private final VendaDaoImpl dao = new VendaDaoImpl();

    private Venda vendaComUmItem() {
        Cliente cliente = new Cliente();
        cliente.setId(3L);

        Produto produto = new Produto();
        produto.setId(9L);
        produto.setCodigo("PRD-009");
        produto.setValor(new BigDecimal("10.00"));

        Venda venda = new Venda();
        venda.setCodigo("V-001");
        venda.setCliente(cliente);
        venda.setDataVenda(DATA);
        venda.setStatus(Venda.Status.INICIADA);
        venda.adicionarProduto(produto, 2);
        return venda;
    }

    @Test
    void cadastrarDeveGravarVendaEItensNaMesmaTransacao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmVenda = mock(PreparedStatement.class);
        PreparedStatement stmItem = mock(PreparedStatement.class);
        ResultSet chaveVenda = mock(ResultSet.class);
        ResultSet chaveItem = mock(ResultSet.class);

        when(conn.prepareStatement(VendaDaoImpl.SQL_INSERT_VENDA, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(stmVenda);
        when(conn.prepareStatement(VendaDaoImpl.SQL_INSERT_ITEM, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(stmItem);
        when(stmVenda.getGeneratedKeys()).thenReturn(chaveVenda);
        when(chaveVenda.next()).thenReturn(true);
        when(chaveVenda.getInt(1)).thenReturn(50);
        when(stmItem.getGeneratedKeys()).thenReturn(chaveItem);
        when(chaveItem.next()).thenReturn(true);
        when(chaveItem.getLong(1)).thenReturn(70L);

        Venda venda = vendaComUmItem();
        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(venda);
        }

        assertEquals(50, id);
        assertEquals(50L, venda.getId());
        assertEquals(70L, venda.getProdutos().iterator().next().getId());

        verify(conn).setAutoCommit(false);
        verify(stmVenda).setString(1, "V-001");
        verify(stmVenda).setLong(2, 3L);
        verify(stmVenda).setBigDecimal(3, new BigDecimal("20.00"));
        verify(stmVenda).setTimestamp(4, Timestamp.from(DATA));
        verify(stmVenda).setString(5, "INICIADA");

        verify(stmItem).setLong(1, 50L);
        verify(stmItem).setLong(2, 9L);
        verify(stmItem).setInt(3, 2);
        verify(stmItem).setBigDecimal(4, new BigDecimal("20.00"));

        verify(conn).commit();
        verify(stmVenda).close();
        verify(stmItem).close();
        verify(conn).close();
    }

    @Test
    void cadastrarDevePreencherDataEStatusQuandoNulos() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet chave = mock(ResultSet.class);
        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(chave);

        Venda venda = vendaComUmItem();
        venda.setDataVenda(null);
        venda.setStatus(null);
        venda.getProdutos().clear();

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            dao.cadastrar(venda);
        }

        assertNotNull(venda.getDataVenda());
        assertEquals(Venda.Status.INICIADA, venda.getStatus());
    }

    @Test
    void cadastrarDeveFazerRollbackEFecharConexaoQuandoItemFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmVenda = mock(PreparedStatement.class);
        PreparedStatement stmItem = mock(PreparedStatement.class);
        ResultSet chaveVenda = mock(ResultSet.class);

        when(conn.prepareStatement(VendaDaoImpl.SQL_INSERT_VENDA, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(stmVenda);
        when(conn.prepareStatement(VendaDaoImpl.SQL_INSERT_ITEM, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(stmItem);
        when(stmVenda.getGeneratedKeys()).thenReturn(chaveVenda);
        when(chaveVenda.next()).thenReturn(true);
        when(chaveVenda.getInt(1)).thenReturn(50);
        when(stmItem.executeUpdate()).thenThrow(new SQLException("violates foreign key constraint"));

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.cadastrar(vendaComUmItem()));
        }

        verify(conn).rollback();
        verify(conn, never()).commit();
        verify(stmVenda).close();
        verify(stmItem).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveAlterarVendaESubstituirItens() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmUpdate = mock(PreparedStatement.class);
        PreparedStatement stmDelete = mock(PreparedStatement.class);
        PreparedStatement stmItem = mock(PreparedStatement.class);
        ResultSet chaveItem = mock(ResultSet.class);

        when(conn.prepareStatement(VendaDaoImpl.SQL_UPDATE_VENDA)).thenReturn(stmUpdate);
        when(conn.prepareStatement(VendaDaoImpl.SQL_DELETE_ITENS)).thenReturn(stmDelete);
        when(conn.prepareStatement(VendaDaoImpl.SQL_INSERT_ITEM, Statement.RETURN_GENERATED_KEYS))
                .thenReturn(stmItem);
        when(stmUpdate.executeUpdate()).thenReturn(1);
        when(stmItem.getGeneratedKeys()).thenReturn(chaveItem);

        Venda venda = vendaComUmItem();
        venda.setId(50L);
        venda.setStatus(Venda.Status.CONCLUIDA);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(venda);
        }

        assertEquals(1, linhas);
        verify(stmUpdate).setString(5, "CONCLUIDA");
        verify(stmUpdate).setLong(6, 50L);
        verify(stmDelete).setLong(1, 50L);
        verify(stmItem).setLong(1, 50L);
        verify(conn).commit();
        verify(conn).close();
    }

    @Test
    void buscaVendaDeveMontarVendaClienteEItens() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmVenda = mock(PreparedStatement.class);
        PreparedStatement stmItens = mock(PreparedStatement.class);
        ResultSet rsVenda = mock(ResultSet.class);
        ResultSet rsItens = mock(ResultSet.class);

        when(conn.prepareStatement(VendaDaoImpl.SQL_SELECT_VENDA + " WHERE v.codigo = ?")).thenReturn(stmVenda);
        when(conn.prepareStatement(VendaDaoImpl.SQL_SELECT_ITENS)).thenReturn(stmItens);
        when(stmVenda.executeQuery()).thenReturn(rsVenda);
        when(stmItens.executeQuery()).thenReturn(rsItens);

        when(rsVenda.next()).thenReturn(true);
        when(rsVenda.getLong("id")).thenReturn(50L);
        when(rsVenda.getString("codigo")).thenReturn("V-001");
        when(rsVenda.getBigDecimal("valor_total")).thenReturn(new BigDecimal("20.00"));
        when(rsVenda.getTimestamp("data_venda")).thenReturn(Timestamp.from(DATA));
        when(rsVenda.getString("status_venda")).thenReturn("CONCLUIDA");
        when(rsVenda.getLong("c_id")).thenReturn(3L);
        when(rsVenda.getString("c_nome")).thenReturn("Joao");
        when(rsVenda.getLong("c_cpf")).thenReturn(12345678901L);
        when(rsVenda.getInt("c_qtd_compras")).thenReturn(6);
        when(rsVenda.getObject("c_data_nascimento", LocalDate.class)).thenReturn(LocalDate.of(1990, 5, 20));

        when(rsItens.next()).thenReturn(true, false);
        when(rsItens.getLong("id")).thenReturn(70L);
        when(rsItens.getInt("quantidade")).thenReturn(2);
        when(rsItens.getBigDecimal("valor_total")).thenReturn(new BigDecimal("20.00"));
        when(rsItens.getLong("p_id")).thenReturn(9L);
        when(rsItens.getString("p_codigo")).thenReturn("PRD-009");
        when(rsItens.getBigDecimal("p_valor")).thenReturn(new BigDecimal("10.00"));
        when(rsItens.getBigDecimal("p_valor_compra")).thenReturn(new BigDecimal("6.00"));
        when(rsItens.getString("p_marca")).thenReturn("Dell");

        Venda venda;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            venda = dao.buscaVenda("V-001");
        }

        assertNotNull(venda);
        assertEquals(50L, venda.getId());
        assertEquals("V-001", venda.getCodigo());
        assertEquals(DATA, venda.getDataVenda());
        assertEquals(Venda.Status.CONCLUIDA, venda.getStatus());
        assertEquals(3L, venda.getCliente().getId());
        assertEquals(12345678901L, venda.getCliente().getCpf());
        assertEquals(6, venda.getCliente().getQtdCompras());
        assertEquals(LocalDate.of(1990, 5, 20), venda.getCliente().getDataNascimento());

        assertEquals(1, venda.getProdutos().size());
        ProdutoQuantidade item = venda.getProdutos().iterator().next();
        assertEquals(70L, item.getId());
        assertEquals(2, item.getQuantidade());
        assertEquals("PRD-009", item.getProduto().getCodigo());
        assertEquals(0, new BigDecimal("6.00").compareTo(item.getProduto().getValorCompra()));
        assertEquals("Dell", item.getProduto().getMarca());
        verify(stmVenda).setString(1, "V-001");
        verify(stmItens).setLong(1, 50L);
        verify(conn).close();
    }

    @Test
    void buscaVendaDeveRetornarNuloQuandoNaoExiste() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Venda venda;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            venda = dao.buscaVenda("INEXISTENTE");
        }

        assertNull(venda);
    }

    @Test
    void excluirDeveRemoverPeloIdEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(VendaDaoImpl.SQL_DELETE_VENDA)).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Venda venda = new Venda();
        venda.setId(50L);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.excluir(venda);
        }

        assertEquals(1, linhas);
        verify(stm).setLong(1, 50L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveFazerRollbackQuandoFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmUpdate = mock(PreparedStatement.class);
        when(conn.prepareStatement(VendaDaoImpl.SQL_UPDATE_VENDA)).thenReturn(stmUpdate);
        when(stmUpdate.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        Venda venda = vendaComUmItem();
        venda.setId(50L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.atualizar(venda));
        }

        verify(conn).rollback();
        verify(conn, never()).commit();
        verify(stmUpdate).close();
        verify(conn).close();
    }

    @Test
    void buscatodosDeveTrazerCadaVendaComSeusItens() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stmVendas = mock(PreparedStatement.class);
        PreparedStatement stmItens = mock(PreparedStatement.class);
        ResultSet rsVendas = mock(ResultSet.class);
        ResultSet rsItensVenda1 = mock(ResultSet.class);
        ResultSet rsItensVenda2 = mock(ResultSet.class);

        when(conn.prepareStatement(VendaDaoImpl.SQL_SELECT_VENDA)).thenReturn(stmVendas);
        when(conn.prepareStatement(VendaDaoImpl.SQL_SELECT_ITENS)).thenReturn(stmItens);
        when(stmVendas.executeQuery()).thenReturn(rsVendas);
        when(stmItens.executeQuery()).thenReturn(rsItensVenda1, rsItensVenda2);

        when(rsVendas.next()).thenReturn(true, true, false);
        when(rsVendas.getLong("id")).thenReturn(1L, 2L);
        when(rsVendas.getString("codigo")).thenReturn("V-1", "V-2");
        when(rsVendas.getTimestamp("data_venda")).thenReturn(Timestamp.from(DATA));
        when(rsVendas.getString("status_venda")).thenReturn("INICIADA", "CANCELADA");

        when(rsItensVenda1.next()).thenReturn(true, true, false);
        when(rsItensVenda1.getLong("id")).thenReturn(10L, 11L);
        when(rsItensVenda1.getString("p_codigo")).thenReturn("A", "B");
        when(rsItensVenda2.next()).thenReturn(false);

        List<Venda> vendas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            vendas = dao.buscatodos();
        }

        assertEquals(2, vendas.size());
        assertEquals("V-1", vendas.get(0).getCodigo());
        assertEquals(2, vendas.get(0).getProdutos().size());
        assertEquals("V-2", vendas.get(1).getCodigo());
        assertEquals(Venda.Status.CANCELADA, vendas.get(1).getStatus());
        assertTrue(vendas.get(1).getProdutos().isEmpty());
        verify(stmItens).setLong(1, 1L);
        verify(stmItens).setLong(1, 2L);
        verify(stmVendas).close();
        verify(conn).close();
    }
}
