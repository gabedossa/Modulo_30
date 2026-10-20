package br.com.ebac;

import br.com.ebac.domain.Produto;
import br.com.ebac.domain.ProdutoQuantidade;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class ProdutoQuantidadeTest {

    private Produto produto;

    @Before
    public void setUp() {
        produto = new Produto();
        produto.setCodigo("P1");
        produto.setValor(new BigDecimal("12.50"));
    }

    @Test
    public void novoItemDeveComecarZerado() {
        ProdutoQuantidade item = new ProdutoQuantidade(produto);

        assertSame(produto, item.getProduto());
        assertEquals(Integer.valueOf(0), item.getQuantidade());
        assertEquals(0, item.getValorTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void deveCalcularValorAoAdicionar() {
        ProdutoQuantidade item = new ProdutoQuantidade(produto);

        item.adicionar(2);
        item.adicionar(1);

        assertEquals(Integer.valueOf(3), item.getQuantidade());
        assertEquals(0, item.getValorTotal().compareTo(new BigDecimal("37.50")));
    }

    @Test
    public void deveRecalcularValorAoRemover() {
        ProdutoQuantidade item = new ProdutoQuantidade(produto);
        item.adicionar(4);

        item.remover(1);

        assertEquals(Integer.valueOf(3), item.getQuantidade());
        assertEquals(0, item.getValorTotal().compareTo(new BigDecimal("37.50")));
    }

    @Test
    public void deveZerarValorAoRemoverTudo() {
        ProdutoQuantidade item = new ProdutoQuantidade(produto);
        item.adicionar(2);

        item.remover(2);

        assertEquals(Integer.valueOf(0), item.getQuantidade());
        assertEquals(0, item.getValorTotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    public void devePermitirPreencherPorSetters() {
        ProdutoQuantidade item = new ProdutoQuantidade();
        item.setProduto(produto);
        item.setQuantidade(5);
        item.setValorTotal(new BigDecimal("62.50"));

        assertSame(produto, item.getProduto());
        assertEquals(Integer.valueOf(5), item.getQuantidade());
        assertEquals(new BigDecimal("62.50"), item.getValorTotal());
    }
}
