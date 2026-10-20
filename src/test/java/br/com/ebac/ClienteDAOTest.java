package br.com.ebac;

import br.com.ebac.dao.ClienteDAO;
import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.domain.Cliente;
import org.junit.After;
import org.junit.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ClienteDAOTest {

    private static final String CPF_1 = "11111111111";
    private static final String CPF_2 = "22222222222";

    private final IClienteDAO dao = new ClienteDAO();

    @After
    public void limpar() throws SQLException {
        for (String cpf : new String[]{CPF_1, CPF_2}) {
            Cliente cliente = dao.buscarPorCpf(cpf);
            if (cliente != null) {
                dao.excluir(cliente);
            }
        }
    }

    private Cliente criarCliente(String cpf) {
        Cliente cliente = new Cliente();
        cliente.setNome("Gabriel");
        cliente.setCpf(cpf);
        cliente.setCidade("São Paulo");
        cliente.setEndereco("Rua das Flores");
        cliente.setEstado("SP");
        cliente.setCasaNumero(123);
        cliente.setTel("11999999999");
        return cliente;
    }

    @Test
    public void deveCadastrarCliente() throws SQLException {
        Cliente cliente = criarCliente(CPF_1);

        assertEquals(Integer.valueOf(1), dao.cadastrar(cliente));
        assertNotNull(cliente.getId());
    }

    @Test(expected = SQLException.class)
    public void naoDeveCadastrarCpfDuplicado() throws SQLException {
        dao.cadastrar(criarCliente(CPF_1));
        dao.cadastrar(criarCliente(CPF_1));
    }

    @Test
    public void deveBuscarClientePorCpf() throws SQLException {
        Cliente cliente = criarCliente(CPF_1);
        dao.cadastrar(cliente);

        Cliente salvo = dao.buscarPorCpf(CPF_1);

        assertNotNull(salvo);
        assertEquals(cliente.getId(), salvo.getId());
        assertEquals("Gabriel", salvo.getNome());
        assertEquals(CPF_1, salvo.getCpf());
        assertEquals("São Paulo", salvo.getCidade());
        assertEquals("Rua das Flores", salvo.getEndereco());
        assertEquals("SP", salvo.getEstado());
        assertEquals(Integer.valueOf(123), salvo.getCasaNumero());
        assertEquals("11999999999", salvo.getTel());
    }

    @Test
    public void deveRetornarNuloQuandoCpfNaoExiste() throws SQLException {
        assertNull(dao.buscarPorCpf(CPF_1));
    }

    @Test
    public void deveBuscarTodosOsClientes() throws SQLException {
        dao.cadastrar(criarCliente(CPF_1));
        dao.cadastrar(criarCliente(CPF_2));

        List<Cliente> clientes = dao.buscarTodos();

        assertTrue(clientes.stream().anyMatch(c -> CPF_1.equals(c.getCpf())));
        assertTrue(clientes.stream().anyMatch(c -> CPF_2.equals(c.getCpf())));
    }

    @Test
    public void deveAtualizarCliente() throws SQLException {
        Cliente cliente = criarCliente(CPF_1);
        dao.cadastrar(cliente);

        cliente.setNome("Gabriel Vinces");
        cliente.setCidade("Campinas");
        cliente.setEndereco("Av. Brasil");
        cliente.setEstado("RJ");
        cliente.setCasaNumero(456);
        cliente.setTel("21988888888");

        assertEquals(Integer.valueOf(1), dao.atualizar(cliente));

        Cliente atualizado = dao.buscarPorCpf(CPF_1);
        assertEquals("Gabriel Vinces", atualizado.getNome());
        assertEquals("Campinas", atualizado.getCidade());
        assertEquals("Av. Brasil", atualizado.getEndereco());
        assertEquals("RJ", atualizado.getEstado());
        assertEquals(Integer.valueOf(456), atualizado.getCasaNumero());
        assertEquals("21988888888", atualizado.getTel());
    }

    @Test
    public void deveExcluirCliente() throws SQLException {
        Cliente cliente = criarCliente(CPF_1);
        dao.cadastrar(cliente);

        assertEquals(Integer.valueOf(1), dao.excluir(cliente));
        assertNull(dao.buscarPorCpf(CPF_1));
    }
}
