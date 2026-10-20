package br.com.ebac;

import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.dao.ProdutoDAO;
import br.com.ebac.domain.Produto;
import org.junit.After;
import org.junit.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ProdutoDAOTest {

    private static final String CODIGO_1 = "TESTE-PROD-1";
    private static final String CODIGO_2 = "TESTE-PROD-2";

    private final IProdutoDAO dao = new ProdutoDAO();

    @After
    public void limpar() throws SQLException {
        for (String codigo : new String[]{CODIGO_1, CODIGO_2}) {
            Produto produto = dao.buscarPorCodigo(codigo);
            if (produto != null) {
                dao.excluir(produto);
            }
        }
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
        Produto produto = criarProduto(CODIGO_1);

        assertEquals(Integer.valueOf(1), dao.cadastrar(produto));
        assertNotNull(produto.getId());
    }

    @Test(expected = SQLException.class)
    public void naoDeveCadastrarCodigoDuplicado() throws SQLException {
        dao.cadastrar(criarProduto(CODIGO_1));
        dao.cadastrar(criarProduto(CODIGO_1));
    }

    @Test
    public void deveBuscarProdutoPorCodigo() throws SQLException {
        Produto produto = criarProduto(CODIGO_1);
        dao.cadastrar(produto);

        Produto salvo = dao.buscarPorCodigo(CODIGO_1);

        assertNotNull(salvo);
        assertEquals(produto.getId(), salvo.getId());
        assertEquals(CODIGO_1, salvo.getCodigo());
        assertEquals("Notebook", salvo.getNome());
        assertEquals("Notebook 16GB", salvo.getDescricao());
        assertEquals(new BigDecimal("3500.00"), salvo.getValor());
    }

    @Test
    public void deveRetornarNuloQuandoCodigoNaoExiste() throws SQLException {
        assertNull(dao.buscarPorCodigo(CODIGO_1));
    }

    @Test
    public void deveBuscarTodosOsProdutos() throws SQLException {
        dao.cadastrar(criarProduto(CODIGO_1));
        dao.cadastrar(criarProduto(CODIGO_2));

        List<Produto> produtos = dao.buscarTodos();

        assertTrue(produtos.stream().anyMatch(p -> CODIGO_1.equals(p.getCodigo())));
        assertTrue(produtos.stream().anyMatch(p -> CODIGO_2.equals(p.getCodigo())));
    }

    @Test
    public void deveAtualizarProduto() throws SQLException {
        Produto produto = criarProduto(CODIGO_1);
        dao.cadastrar(produto);

        produto.setNome("Notebook Gamer");
        produto.setDescricao("Notebook 32GB");
        produto.setValor(new BigDecimal("5999.90"));

        assertEquals(Integer.valueOf(1), dao.atualizar(produto));

        Produto atualizado = dao.buscarPorCodigo(CODIGO_1);
        assertEquals("Notebook Gamer", atualizado.getNome());
        assertEquals("Notebook 32GB", atualizado.getDescricao());
        assertEquals(new BigDecimal("5999.90"), atualizado.getValor());
    }

    @Test
    public void deveExcluirProduto() throws SQLException {
        Produto produto = criarProduto(CODIGO_1);
        dao.cadastrar(produto);

        assertEquals(Integer.valueOf(1), dao.excluir(produto));
        assertNull(dao.buscarPorCodigo(CODIGO_1));
    }
}
