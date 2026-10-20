package br.com.ebac;

import br.com.ebac.domain.Produto;
import br.com.ebac.service.IProdutoService;
import br.com.ebac.service.ProdutoService;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProdutoServiceTest {

    private IProdutoService service;

    @Before
    public void setUp() {
        service = new ProdutoService(new ProdutoDAOMock());
    }

    private Produto criarProduto(String codigo) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome("Notebook");
        produto.setDescricao("Notebook 16GB");
        produto.setValor(new BigDecimal("3500.00"));
        return produto;
    }

    @Test
    public void deveCadastrarProduto() throws SQLException {
        Produto produto = service.cadastrar(criarProduto("P1"));

        assertNotNull(produto.getId());
        assertNotNull(service.consultar("P1"));
    }

    @Test
    public void deveNormalizarCodigo() throws SQLException {
        Produto produto = service.cadastrar(criarProduto("  p1 "));

        assertEquals("P1", produto.getCodigo());
        assertNotNull(service.consultar("p1"));
    }

    @Test
    public void deveRetornarNuloQuandoCodigoNaoExiste() throws SQLException {
        assertNull(service.consultar("P99"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarProdutoNulo() throws SQLException {
        service.cadastrar(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemCodigo() throws SQLException {
        service.cadastrar(criarProduto(" "));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemNome() throws SQLException {
        Produto produto = criarProduto("P1");
        produto.setNome(null);
        service.cadastrar(produto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarSemValor() throws SQLException {
        Produto produto = criarProduto("P1");
        produto.setValor(null);
        service.cadastrar(produto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarValorZero() throws SQLException {
        Produto produto = criarProduto("P1");
        produto.setValor(BigDecimal.ZERO);
        service.cadastrar(produto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveCadastrarCodigoDuplicado() throws SQLException {
        service.cadastrar(criarProduto("P1"));
        service.cadastrar(criarProduto("P1"));
    }

    @Test
    public void deveBuscarTodosOsProdutos() throws SQLException {
        service.cadastrar(criarProduto("P1"));
        service.cadastrar(criarProduto("P2"));

        assertEquals(2, service.buscarTodos().size());
    }

    @Test
    public void deveAtualizarProduto() throws SQLException {
        Produto produto = service.cadastrar(criarProduto("P1"));
        produto.setValor(new BigDecimal("2999.90"));

        service.atualizar(produto);

        assertEquals(new BigDecimal("2999.90"), service.consultar("P1").getValor());
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarProdutoSemId() throws SQLException {
        service.atualizar(criarProduto("P1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarProdutoInexistente() throws SQLException {
        Produto produto = criarProduto("P1");
        produto.setId(99L);
        service.atualizar(produto);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveAtualizarParaCodigoDeOutroProduto() throws SQLException {
        service.cadastrar(criarProduto("P1"));
        Produto outro = service.cadastrar(criarProduto("P2"));

        outro.setCodigo("P1");
        service.atualizar(outro);
    }

    @Test
    public void deveExcluirProduto() throws SQLException {
        service.cadastrar(criarProduto("P1"));

        service.excluir("P1");

        assertNull(service.consultar("P1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void naoDeveExcluirProdutoInexistente() throws SQLException {
        service.excluir("P99");
    }
}
