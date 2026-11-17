package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Produto;

class ProdutoDaoImplTest {

    private final ProdutoDaoImpl dao = new ProdutoDaoImpl();
    private final List<Produto> criados = new ArrayList<>();

    private Produto novoProduto(String nome, String descricao, BigDecimal valor) throws Exception {
        Produto produto = new Produto();
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setNome(nome);
        produto.setDescricao(descricao);
        produto.setValor(valor);
        produto.setValorCompra(new BigDecimal("1.00"));
        Integer id = dao.cadastrar(produto);
        produto.setId(id.longValue());
        criados.add(produto);
        return produto;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Produto produto : criados) {
            try {
                dao.excluir(produto);
            } catch (Exception e) {

            }
        }
        criados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirProduto() throws Exception {
        Produto produto = novoProduto("Teclado", "Teclado mecanico", new BigDecimal("199.90"));

        assertNotNull(produto.getId());

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNotNull(encontrado);
        assertEquals(produto.getCodigo(), encontrado.getCodigo());
        assertEquals("Teclado", encontrado.getNome());
        assertEquals("Teclado mecanico", encontrado.getDescricao());
        assertEquals(0, new BigDecimal("199.90").compareTo(encontrado.getValor()));
        assertEquals(0, new BigDecimal("1.00").compareTo(encontrado.getValorCompra()));
        assertEquals(true, encontrado.getDisponivel());
    }

    @Test
    void atualizarDeveAlterarOsDadosDoProduto() throws Exception {
        Produto produto = novoProduto("Mouse", "Mouse com fio", new BigDecimal("59.90"));

        produto.setNome("Mouse Atualizado");
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setDescricao("Mouse sem fio");
        produto.setValor(new BigDecimal("69.90"));
        produto.setValorCompra(new BigDecimal("35.00"));
        produto.setDisponivel(false);
        Integer linhasAfetadas = dao.atualizar(produto);

        assertEquals(1, linhasAfetadas);

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNotNull(encontrado);
        assertEquals("Mouse Atualizado", encontrado.getNome());
        assertEquals("Mouse sem fio", encontrado.getDescricao());
        assertEquals(0, new BigDecimal("69.90").compareTo(encontrado.getValor()));
        assertEquals(0, new BigDecimal("35.00").compareTo(encontrado.getValorCompra()));
        assertEquals(false, encontrado.getDisponivel());
    }

    @Test
    void buscaProdutoDeveRetornarNuloQuandoCodigoNaoExiste() throws Exception {
        Produto encontrado = dao.buscaProduto("CODIGO-INEXISTENTE-" + UUID.randomUUID());
        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveIncluirProdutosCadastrados() throws Exception {
        Produto produto1 = novoProduto("Produto Um", "Primeiro", new BigDecimal("10.00"));
        Produto produto2 = novoProduto("Produto Dois", "Segundo", new BigDecimal("20.00"));

        List<Produto> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(p -> p.getCodigo().equals(produto1.getCodigo())));
        assertTrue(todos.stream().anyMatch(p -> p.getCodigo().equals(produto2.getCodigo())));
    }

    @Test
    void excluirDeveRemoverProdutoDoBanco() throws Exception {
        Produto produto = novoProduto("Produto Para Excluir", "Sera excluido", new BigDecimal("5.00"));

        Integer linhasAfetadas = dao.excluir(produto);
        assertEquals(1, linhasAfetadas);

        Produto encontrado = dao.buscaProduto(produto.getCodigo());
        assertNull(encontrado);

        criados.remove(produto);
    }

    public ProdutoDaoImpl getDao() {
        return dao;
    }
}
