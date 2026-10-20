package br.com.ebac;

import br.com.ebac.dao.ClienteDAO;
import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.dao.IVendaDAO;
import br.com.ebac.dao.ProdutoDAO;
import br.com.ebac.dao.VendaDAO;
import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.Produto;
import br.com.ebac.domain.Venda;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class VendaDAOTest {

    private static final String CPF = "33333333333";
    private static final String CODIGO_VENDA_1 = "TESTE-VENDA-1";
    private static final String CODIGO_VENDA_2 = "TESTE-VENDA-2";

    private final IVendaDAO vendaDAO = new VendaDAO();
    private final IClienteDAO clienteDAO = new ClienteDAO();
    private final IProdutoDAO produtoDAO = new ProdutoDAO();

    private Cliente cliente;
    private Produto mouse;
    private Produto teclado;

    @Before
    public void setUp() throws SQLException {
        cliente = new Cliente();
        cliente.setNome("Cliente Venda");
        cliente.setCpf(CPF);
        clienteDAO.cadastrar(cliente);

        mouse = criarProduto("TESTE-MOUSE", "50.00");
        teclado = criarProduto("TESTE-TECLADO", "120.00");
    }

    @After
    public void limpar() throws SQLException {
        for (String codigo : new String[]{CODIGO_VENDA_1, CODIGO_VENDA_2}) {
            Venda venda = vendaDAO.buscarPorCodigo(codigo);
            if (venda != null) {
                vendaDAO.excluir(venda);
            }
        }
        produtoDAO.excluir(mouse);
        produtoDAO.excluir(teclado);
        clienteDAO.excluir(cliente);
    }

    private Produto criarProduto(String codigo, String valor) throws SQLException {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome(codigo);
        produto.setValor(new BigDecimal(valor));
        produtoDAO.cadastrar(produto);
        return produto;
    }

    private Venda criarVenda(String codigo) {
        Venda venda = new Venda();
        venda.setCodigo(codigo);
        venda.setCliente(cliente);
        venda.setDataVenda(LocalDateTime.of(2026, 10, 20, 15, 30));
        venda.setStatus(Venda.Status.INICIADA);
        venda.adicionarProduto(mouse, 2);
        venda.adicionarProduto(teclado, 1);
        return venda;
    }

    @Test
    public void deveCadastrarVenda() throws SQLException {
        Venda venda = criarVenda(CODIGO_VENDA_1);

        assertEquals(Integer.valueOf(1), vendaDAO.cadastrar(venda));
        assertNotNull(venda.getId());
    }

    @Test(expected = SQLException.class)
    public void naoDeveCadastrarCodigoDuplicado() throws SQLException {
        vendaDAO.cadastrar(criarVenda(CODIGO_VENDA_1));
        vendaDAO.cadastrar(criarVenda(CODIGO_VENDA_1));
    }

    @Test
    public void deveBuscarVendaComClienteEProdutos() throws SQLException {
        Venda venda = criarVenda(CODIGO_VENDA_1);
        vendaDAO.cadastrar(venda);

        Venda salva = vendaDAO.buscarPorCodigo(CODIGO_VENDA_1);

        assertNotNull(salva);
        assertEquals(venda.getId(), salva.getId());
        assertEquals(Venda.Status.INICIADA, salva.getStatus());
        assertEquals(LocalDateTime.of(2026, 10, 20, 15, 30), salva.getDataVenda());
        assertEquals(new BigDecimal("220.00"), salva.getValorTotal());
        assertEquals(CPF, salva.getCliente().getCpf());
        assertEquals(2, salva.getProdutos().size());
        assertEquals(Integer.valueOf(2), salva.buscarItem("TESTE-MOUSE").getQuantidade());
        assertEquals(new BigDecimal("100.00"), salva.buscarItem("TESTE-MOUSE").getValorTotal());
        assertEquals(Integer.valueOf(1), salva.buscarItem("TESTE-TECLADO").getQuantidade());
    }

    @Test
    public void deveRetornarNuloQuandoCodigoNaoExiste() throws SQLException {
        assertNull(vendaDAO.buscarPorCodigo(CODIGO_VENDA_1));
    }

    @Test
    public void deveBuscarTodasAsVendas() throws SQLException {
        vendaDAO.cadastrar(criarVenda(CODIGO_VENDA_1));
        vendaDAO.cadastrar(criarVenda(CODIGO_VENDA_2));

        List<Venda> vendas = vendaDAO.buscarTodos();

        assertTrue(vendas.stream().anyMatch(v -> CODIGO_VENDA_1.equals(v.getCodigo())));
        assertTrue(vendas.stream().anyMatch(v -> CODIGO_VENDA_2.equals(v.getCodigo())));
    }

    @Test
    public void deveAtualizarStatusEProdutos() throws SQLException {
        Venda venda = criarVenda(CODIGO_VENDA_1);
        vendaDAO.cadastrar(venda);

        venda.removerProduto("TESTE-TECLADO", 1);
        venda.adicionarProduto(mouse, 1);
        venda.setStatus(Venda.Status.CONCLUIDA);

        assertEquals(Integer.valueOf(1), vendaDAO.atualizar(venda));

        Venda atualizada = vendaDAO.buscarPorCodigo(CODIGO_VENDA_1);
        assertEquals(Venda.Status.CONCLUIDA, atualizada.getStatus());
        assertEquals(1, atualizada.getProdutos().size());
        assertEquals(Integer.valueOf(3), atualizada.buscarItem("TESTE-MOUSE").getQuantidade());
        assertNull(atualizada.buscarItem("TESTE-TECLADO"));
        assertEquals(new BigDecimal("150.00"), atualizada.getValorTotal());
    }

    @Test
    public void deveExcluirVendaComSeusItens() throws SQLException {
        Venda venda = criarVenda(CODIGO_VENDA_1);
        vendaDAO.cadastrar(venda);

        assertEquals(Integer.valueOf(1), vendaDAO.excluir(venda));
        assertNull(vendaDAO.buscarPorCodigo(CODIGO_VENDA_1));
    }
}
