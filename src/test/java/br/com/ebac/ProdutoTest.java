package br.com.ebac;

import br.com.ebac.domain.Produto;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ProdutoTest {

    @Test
    public void deveGuardarOsDadosDoProduto() {
        Produto produto = new Produto();
        produto.setId(1L);
        produto.setCodigo("P1");
        produto.setNome("Notebook");
        produto.setDescricao("Notebook 16GB");
        produto.setValor(new BigDecimal("3500.00"));

        assertEquals(Long.valueOf(1L), produto.getId());
        assertEquals("P1", produto.getCodigo());
        assertEquals("Notebook", produto.getNome());
        assertEquals("Notebook 16GB", produto.getDescricao());
        assertEquals(new BigDecimal("3500.00"), produto.getValor());
    }

    @Test
    public void novoProdutoDeveComecarVazio() {
        Produto produto = new Produto();

        assertNull(produto.getId());
        assertNull(produto.getCodigo());
        assertNull(produto.getNome());
        assertNull(produto.getDescricao());
        assertNull(produto.getValor());
    }
}
