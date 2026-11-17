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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import factory.ConnectionFactory;
import model.Cliente;

class ClienteDaoImplUnitTest {

    private static final String COLUNAS =
            "id, nome, cpf, tel, endereco, numero, cidade, estado, email, qtd_compras, data_nascimento";

    private static final LocalDate NASCIMENTO = LocalDate.of(1990, 5, 20);

    private final ClienteDaoImpl dao = new ClienteDaoImpl();

    private Cliente clienteCompleto() {
        Cliente cliente = new Cliente();
        cliente.setNome("Joao da Silva");
        cliente.setCpf(12345678901L);
        cliente.setTel(11999998888L);
        cliente.setEnd("Rua das Flores");
        cliente.setNumero(100);
        cliente.setCidade("Sao Paulo");
        cliente.setEstado("SP");
        cliente.setEmail("joao@email.com");
        cliente.setQtdCompras(4);
        cliente.setDataNascimento(NASCIMENTO);
        return cliente;
    }

    private void verificaParametrosDoCliente(PreparedStatement stm) throws Exception {
        verify(stm).setString(1, "Joao da Silva");
        verify(stm).setLong(2, 12345678901L);
        verify(stm).setObject(3, 11999998888L, Types.BIGINT);
        verify(stm).setString(4, "Rua das Flores");
        verify(stm).setObject(5, 100, Types.INTEGER);
        verify(stm).setString(6, "Sao Paulo");
        verify(stm).setString(7, "SP");
        verify(stm).setString(8, "joao@email.com");
        verify(stm).setInt(9, 4);
        verify(stm).setObject(10, NASCIMENTO, Types.DATE);
    }

    @Test
    void cadastrarDeveInserirClienteEFecharConexao() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(7);

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(clienteCompleto());
        }

        assertEquals(7, id);
        verify(conn).prepareStatement(
                "INSERT INTO tb_cliente (nome, cpf, tel, endereco, numero, cidade, estado, email, qtd_compras, "
                        + "data_nascimento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);
        verificaParametrosDoCliente(stm);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void cadastrarDeveAceitarTelefoneENumeroNulos() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);

        Cliente cliente = new Cliente();
        cliente.setNome("Sem Telefone");
        cliente.setCpf(1L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            dao.cadastrar(cliente);
        }

        verify(stm).setObject(3, null, Types.BIGINT);
        verify(stm).setObject(5, null, Types.INTEGER);
        verify(stm).setInt(9, 0);
        verify(stm).setObject(10, null, Types.DATE);
    }

    @Test
    void atualizarDeveAtualizarTodosOsCamposPeloId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Cliente cliente = clienteCompleto();
        cliente.setId(3L);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.atualizar(cliente);
        }

        assertEquals(1, linhas);
        verify(conn).prepareStatement("UPDATE tb_cliente SET nome = ?, cpf = ?, tel = ?, endereco = ?, numero = ?, "
                + "cidade = ?, estado = ?, email = ?, qtd_compras = ?, data_nascimento = ? WHERE id = ?");
        verificaParametrosDoCliente(stm);
        verify(stm).setLong(11, 3L);
        verify(stm).close();
        verify(conn).close();
    }

    @Test
    void buscaClienteDeveMontarObjetoAPartirDoResultSet() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(true);
        when(rs.getLong("id")).thenReturn(5L);
        when(rs.getString("nome")).thenReturn("Cliente Cinco");
        when(rs.getLong("cpf")).thenReturn(55555555555L);
        when(rs.getObject("tel", Long.class)).thenReturn(1133334444L);
        when(rs.getString("endereco")).thenReturn("Av. Brasil");
        when(rs.getObject("numero", Integer.class)).thenReturn(50);
        when(rs.getString("cidade")).thenReturn("Curitiba");
        when(rs.getString("estado")).thenReturn("PR");
        when(rs.getString("email")).thenReturn("cinco@email.com");
        when(rs.getInt("qtd_compras")).thenReturn(7);
        when(rs.getObject("data_nascimento", LocalDate.class)).thenReturn(NASCIMENTO);

        Cliente encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaCliente(55555555555L);
        }

        assertNotNull(encontrado);
        assertEquals(5L, encontrado.getId());
        assertEquals("Cliente Cinco", encontrado.getNome());
        assertEquals(55555555555L, encontrado.getCpf());
        assertEquals(1133334444L, encontrado.getTel());
        assertEquals("Av. Brasil", encontrado.getEnd());
        assertEquals(50, encontrado.getNumero());
        assertEquals("Curitiba", encontrado.getCidade());
        assertEquals("PR", encontrado.getEstado());
        assertEquals("cinco@email.com", encontrado.getEmail());
        assertEquals(7, encontrado.getQtdCompras());
        assertEquals(NASCIMENTO, encontrado.getDataNascimento());
        verify(conn).prepareStatement("SELECT " + COLUNAS + " FROM tb_cliente WHERE cpf = ?");
        verify(stm).setLong(1, 55555555555L);
    }

    @Test
    void buscaClienteDeveRetornarNuloQuandoResultSetVazio() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        Cliente encontrado;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            encontrado = dao.buscaCliente(0L);
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
        when(rs.getLong("cpf")).thenReturn(11111111111L, 22222222222L);
        when(rs.getString("nome")).thenReturn("Cliente Um", "Cliente Dois");

        List<Cliente> todos;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            todos = dao.buscatodos();
        }

        assertEquals(2, todos.size());
        assertEquals(11111111111L, todos.get(0).getCpf());
        assertEquals(22222222222L, todos.get(1).getCpf());
        verify(conn).prepareStatement("SELECT " + COLUNAS + " FROM tb_cliente");
    }

    @Test
    void cadastrarDeveRetornarNuloQuandoBancoNaoDevolveId() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);
        when(conn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(stm);
        when(stm.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(false);

        Integer id;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            id = dao.cadastrar(clienteCompleto());
        }

        assertNull(id);
        verify(conn).close();
    }

    @Test
    void excluirDeveRemoverPeloIdEDevolverLinhasAfetadas() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement stm = mock(PreparedStatement.class);
        when(conn.prepareStatement(anyString())).thenReturn(stm);
        when(stm.executeUpdate()).thenReturn(1);

        Cliente cliente = new Cliente();
        cliente.setId(8L);

        Integer linhas;
        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            linhas = dao.excluir(cliente);
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
        when(stm.executeUpdate()).thenThrow(new SQLException("falha simulada"));

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        try (MockedStatic<ConnectionFactory> factory = mockStatic(ConnectionFactory.class)) {
            factory.when(ConnectionFactory::getConnection).thenReturn(conn);
            assertThrows(SQLException.class, () -> dao.excluir(cliente));
        }

        verify(conn).prepareStatement("DELETE FROM tb_cliente WHERE id = ?");
        verify(stm).close();
        verify(conn).close();
    }
}
