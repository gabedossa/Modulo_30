package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.Produto;

class ProdutoDaoImplUnitTest {

    private static final String COLUNAS = "id, codigo, nome, descricao, valor, valor_compra, disponivel";

    private final ProdutoDaoImpl dao = new ProdutoDaoImpl();

    @Test
    void cadastrarDeveInserirProdutoEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(11);

        Produto produto = new Produto();
        produto.setCodigo("PRD-001");
        produto.setNome("Teclado");
        produto.setDescricao("Teclado mecanico ABNT2");
        produto.setValor(new BigDecimal("199.90"));
        produto.setValorCompra(new BigDecimal("120.00"));
        produto.setDisponivel(false);

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(produto);
        }

        assertEquals(11, id);
        verify(conn).prepareStatement(
                "INSERT INTO tb_produto (codigo, nome, descricao, valor, valor_compra, disponivel) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        verify(stm).setString(1, "PRD-001");
        verify(stm).setString(2, "Teclado");
        verify(stm).setString(3, "Teclado mecanico ABNT2");
        verify(stm).setBigDecimal(4, new BigDecimal("199.90"));
        verify(stm).setBigDecimal(5, new BigDecimal("120.00"));
        verify(stm).setBoolean(6, false);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void atualizarDeveAtualizarTodosOsCamposPeloId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Produto produto = new Produto();
        produto.setId(4L);
        produto.setCodigo("PRD-002");
        produto.setNome("Mouse");
        produto.setDescricao("Mouse sem fio");
        produto.setValor(new BigDecimal("69.90"));
        produto.setValorCompra(new BigDecimal("40.00"));
        produto.setDisponivel(true);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(produto);
        }

        assertEquals(1, linhas);
        verify(conn).prepareStatement(
                "UPDATE tb_produto SET codigo = ?, nome = ?, descricao = ?, valor = ?, valor_compra = ?, "
                        + "disponivel = ? WHERE id = ?");
        verify(stm).setString(1, "PRD-002");
        verify(stm).setString(2, "Mouse");
        verify(stm).setString(3, "Mouse sem fio");
        verify(stm).setBigDecimal(4, new BigDecimal("69.90"));
        verify(stm).setBigDecimal(5, new BigDecimal("40.00"));
        verify(stm).setBoolean(6, true);
        verify(stm).setLong(7, 4L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void buscaProdutoDeveMontarObjetoAPartirDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(8L);
        when(rs.getString("codigo")).thenReturn("PRD-008");
        when(rs.getString("nome")).thenReturn("Monitor");
        when(rs.getString("descricao")).thenReturn("Monitor 24 polegadas");
        when(rs.getBigDecimal("valor")).thenReturn(new BigDecimal("899.00"));
        when(rs.getBigDecimal("valor_compra")).thenReturn(new BigDecimal("650.00"));
        when(rs.getBoolean("disponivel")).thenReturn(false);

        Produto encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaProduto("PRD-008");
        }

        assertNotNull(encontrado);
        assertEquals(8L, encontrado.getId());
        assertEquals("PRD-008", encontrado.getCodigo());
        assertEquals("Monitor", encontrado.getNome());
        assertEquals("Monitor 24 polegadas", encontrado.getDescricao());
        assertEquals(0, new BigDecimal("899.00").compareTo(encontrado.getValor()));
        assertEquals(0, new BigDecimal("650.00").compareTo(encontrado.getValorCompra()));
        assertEquals(false, encontrado.getDisponivel());
        verify(conn).prepareStatement("SELECT " + COLUNAS + " FROM tb_produto WHERE codigo = ?");
        verify(stm).setString(1, "PRD-008");
    }

    @Test
    void buscaProdutoDeveRetornarNuloQuandoResultSetVazio() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Produto encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaProduto("INEXISTENTE");
        }

        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveMapearTodasAsLinhasDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true, true, false);
        when(rs.getLong("id")).thenReturn(1L, 2L);
        when(rs.getString("codigo")).thenReturn("PRD-001", "PRD-002");
        when(rs.getString("nome")).thenReturn("Produto Um", "Produto Dois");
        when(rs.getBigDecimal("valor")).thenReturn(new BigDecimal("10.00"), new BigDecimal("20.00"));

        List<Produto> todos;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            todos = dao.buscatodos();
        }

        assertEquals(2, todos.size());
        assertEquals("PRD-001", todos.get(0).getCodigo());
        assertEquals("PRD-002", todos.get(1).getCodigo());
        verify(conn).prepareStatement("SELECT " + COLUNAS + " FROM tb_produto");
    }

    @Test
    void cadastrarDeveFecharConexaoQuandoInsertFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.executeUpdate()).thenThrow(new SQLException("duplicate key value violates unique constraint"));

        Produto produto = new Produto();
        produto.setCodigo("PRD-DUPLICADO");
        produto.setValor(BigDecimal.ONE);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.cadastrar(produto));
        }

        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void excluirDeveRemoverPeloIdEDevolverLinhasAfetadas() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Produto produto = new Produto();
        produto.setId(8L);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.excluir(produto);
        }

        assertEquals(1, linhas);
        verify(stm).setLong(1, 8L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void excluirDeveFecharConexaoMesmoQuandoExecuteUpdateFalha() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenThrow(new SQLException("violates foreign key constraint"));

        Produto produto = new Produto();
        produto.setId(1L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.excluir(produto));
        }

        verify(conn).prepareStatement("DELETE FROM tb_produto WHERE id = ?");
        verify(stm).close();
        verify(conn).close();
    }
}
