package br.com.ebac;

import br.com.ebac.domain.Cliente;
import br.com.ebac.service.ClienteService;
import br.com.ebac.service.IClienteService;
import org.junit.Before;
import org.junit.Test;

import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ClienteServiceTest {

    private IClienteService service;

    @Before
    public void setUp() {
        service = new ClienteService(new ClienteDAOMock());
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
        Cliente cliente = service.cadastrar(criarCliente("11111111111"));

        assertNotNull(cliente.getId());
        assertNotNull(service.buscarPorCpf("11111111111"));
    }

    @Test
    public void deveNormalizarCpfEEstadoAoCadastrar() throws SQLException {
        Cliente cliente = criarCliente("111.111.111-11");
        cliente.setEstado(" sp ");

        service.cadastrar(cliente);

        assertEquals("11111111111", cliente.getCpf());
        assertEquals("SP", cliente.getEstado());
    }

    @Test
    public void deveBuscarPorCpfComMascara() throws SQLException {
        service.cadastrar(criarCliente("11111111111"));

        assertNotNull(service.buscarPorCpf("111.111.111-11"));
    }

    @Test
    public void deveRetornarNuloQuandoCpfNaoExiste() throws SQLException {
        assertNull(service.buscarPorCpf("99999999999"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarClienteNulo() throws SQLException {
        service.cadastrar(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemNome() throws SQLException {
        Cliente cliente = criarCliente("11111111111");
        cliente.setNome("  ");
        service.cadastrar(cliente);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarCpfInvalido() throws SQLException {
        service.cadastrar(criarCliente("123"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemCpf() throws SQLException {
        service.cadastrar(criarCliente(null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarEstadoInvalido() throws SQLException {
        Cliente cliente = criarCliente("11111111111");
        cliente.setEstado("São Paulo");
        service.cadastrar(cliente);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarNumeroCasaNegativo() throws SQLException {
        Cliente cliente = criarCliente("11111111111");
        cliente.setCasaNumero(-1);
        service.cadastrar(cliente);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarCpfDuplicado() throws SQLException {
        service.cadastrar(criarCliente("11111111111"));
        service.cadastrar(criarCliente("11111111111"));
    }

    @Test
    public void deveBuscarTodosOsClientes() throws SQLException {
        service.cadastrar(criarCliente("11111111111"));
        service.cadastrar(criarCliente("22222222222"));

        assertEquals(2, service.buscarTodos().size());
    }

    @Test
    public void deveAtualizarCliente() throws SQLException {
        Cliente cliente = service.cadastrar(criarCliente("11111111111"));
        cliente.setCidade("Campinas");

        service.atualizar(cliente);

        assertEquals("Campinas", service.buscarPorCpf("11111111111").getCidade());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarClienteSemId() throws SQLException {
        service.atualizar(criarCliente("11111111111"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarClienteInexistente() throws SQLException {
        Cliente cliente = criarCliente("11111111111");
        cliente.setId(99L);
        service.atualizar(cliente);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarParaCpfDeOutroCliente() throws SQLException {
        service.cadastrar(criarCliente("11111111111"));
        Cliente outro = service.cadastrar(criarCliente("22222222222"));

        outro.setCpf("11111111111");
        service.atualizar(outro);
    }

    @Test
    public void deveExcluirCliente() throws SQLException {
        service.cadastrar(criarCliente("11111111111"));

        service.excluir("11111111111");

        assertNull(service.buscarPorCpf("11111111111"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveExcluirClienteInexistente() throws SQLException {
        service.excluir("99999999999");
    }
}
