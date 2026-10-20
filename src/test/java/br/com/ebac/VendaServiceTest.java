package br.com.ebac;

import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.Produto;
import br.com.ebac.domain.Venda;
import br.com.ebac.service.IVendaService;
import br.com.ebac.service.VendaService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VendaServiceTest {

    private static final String CPF = "11111111111";

    private IVendaService service;

    @Before
    public void setUp() {
        ClienteDAOMock clienteDAO = new ClienteDAOMock();
        Cliente cliente = new Cliente();
        cliente.setNome("Gabriel");
        cliente.setCpf(CPF);
        clienteDAO.cadastrar(cliente);

        ProdutoDAOMock produtoDAO = new ProdutoDAOMock();
        produtoDAO.cadastrar(criarProduto("MOUSE", "50.00"));
        produtoDAO.cadastrar(criarProduto("TECLADO", "120.00"));

        service = new VendaService(new VendaDAOMock(), clienteDAO, produtoDAO);
    }

    private Produto criarProduto(String codigo, String valor) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome(codigo);
        produto.setValor(new BigDecimal(valor));
        return produto;
    }

    private Venda criarVenda(String codigo) {
        Cliente cliente = new Cliente();
        cliente.setCpf(CPF);

        Venda venda = new Venda();
        venda.setCodigo(codigo);
        venda.setCliente(cliente);
        venda.adicionarProduto(criarProduto("MOUSE", "50.00"), 2);
        return venda;
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual));
    }

    @Test
    public void deveCadastrarVendaIniciada() throws SQLException {
        Venda venda = service.cadastrar(criarVenda("V1"));

        assertNotNull(venda.getId());
        assertNotNull(venda.getCliente().getId());
        assertNotNull(venda.getDataVenda());
        assertEquals(Venda.Status.INICIADA, venda.getStatus());
        assertValor("100.00", venda.getValorTotal());
    }

    @Test
    public void deveUsarValorDoProdutoCadastrado() throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setCpf(CPF);
        Venda venda = new Venda();
        venda.setCodigo("V1");
        venda.setCliente(cliente);
        venda.adicionarProduto(criarProduto("MOUSE", "1.00"), 1);

        service.cadastrar(venda);

        assertNotNull(venda.buscarItem("MOUSE").getProduto().getId());
        assertValor("50.00", venda.getValorTotal());
    }

    @Test
    public void deveCadastrarVendaSemProdutos() throws SQLException {
        Venda venda = criarVenda("V1");
        venda.setProdutos(new java.util.ArrayList<>());

        service.cadastrar(venda);

        assertValor("0", venda.getValorTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarVendaNula() throws SQLException {
        service.cadastrar(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemCodigo() throws SQLException {
        service.cadastrar(criarVenda(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarCodigoDuplicado() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.cadastrar(criarVenda("V1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemCliente() throws SQLException {
        Venda venda = criarVenda("V1");
        venda.setCliente(null);
        service.cadastrar(venda);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarComClienteNaoCadastrado() throws SQLException {
        Venda venda = criarVenda("V1");
        venda.getCliente().setCpf("99999999999");
        service.cadastrar(venda);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarComProdutoNaoCadastrado() throws SQLException {
        Venda venda = criarVenda("V1");
        venda.adicionarProduto(criarProduto("MONITOR", "900.00"), 1);
        service.cadastrar(venda);
    }

    @Test
    public void deveBuscarVendaPorCodigo() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        assertNotNull(service.consultar("v1"));
        assertNull(service.consultar("V99"));
    }

    @Test
    public void deveBuscarTodasAsVendas() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.cadastrar(criarVenda("V2"));

        assertEquals(2, service.buscarTodos().size());
    }

    @Test
    public void deveAdicionarProduto() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        Venda venda = service.adicionarProduto("V1", "TECLADO", 1);

        assertEquals(2, venda.getProdutos().size());
        assertValor("220.00", venda.getValorTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAdicionarProdutoNaoCadastrado() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.adicionarProduto("V1", "MONITOR", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAdicionarQuantidadeZero() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.adicionarProduto("V1", "TECLADO", 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAdicionarEmVendaInexistente() throws SQLException {
        service.adicionarProduto("V99", "TECLADO", 1);
    }

    @Test
    public void deveRemoverProduto() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        Venda venda = service.removerProduto("V1", "MOUSE", 1);

        assertEquals(Integer.valueOf(1), venda.buscarItem("MOUSE").getQuantidade());
        assertValor("50.00", venda.getValorTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveRemoverProdutoForaDaVenda() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.removerProduto("V1", "TECLADO", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveRemoverMaisQueAQuantidade() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.removerProduto("V1", "MOUSE", 3);
    }

    @Test
    public void deveFinalizarVenda() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        Venda venda = service.finalizar("V1");

        assertEquals(Venda.Status.CONCLUIDA, venda.getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveFinalizarVendaSemProdutos() throws SQLException {
        Venda venda = criarVenda("V1");
        venda.setProdutos(new java.util.ArrayList<>());
        service.cadastrar(venda);

        service.finalizar("V1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAlterarProdutosDeVendaConcluida() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.finalizar("V1");

        service.adicionarProduto("V1", "TECLADO", 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveFinalizarDuasVezes() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.finalizar("V1");
        service.finalizar("V1");
    }

    @Test
    public void deveCancelarVenda() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        assertEquals(Venda.Status.CANCELADA, service.cancelar("V1").getStatus());
    }

    @Test
    public void deveCancelarVendaConcluida() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.finalizar("V1");

        assertEquals(Venda.Status.CANCELADA, service.cancelar("V1").getStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCancelarDuasVezes() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.cancelar("V1");
        service.cancelar("V1");
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveFinalizarVendaCancelada() throws SQLException {
        service.cadastrar(criarVenda("V1"));
        service.cancelar("V1");
        service.finalizar("V1");
    }

    @Test
    public void deveExcluirVenda() throws SQLException {
        service.cadastrar(criarVenda("V1"));

        service.excluir("V1");

        assertTrue(service.buscarTodos().isEmpty());
    }

    @Test
    public void deveAtualizarVendaIniciada() throws SQLException {
        Venda venda = service.cadastrar(criarVenda("V1"));
        venda.adicionarProduto(criarProduto("TECLADO", "120.00"), 1);

        service.atualizar(venda);

        assertEquals(2, service.consultar("V1").getProdutos().size());
        assertValor("220.00", service.consultar("V1").getValorTotal());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarVendaConcluida() throws SQLException {
        Venda venda = service.cadastrar(criarVenda("V1"));
        service.finalizar("V1");

        service.atualizar(venda);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveExcluirVendaInexistente() throws SQLException {
        service.excluir("V99");
    }
}
