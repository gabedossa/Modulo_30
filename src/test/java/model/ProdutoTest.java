package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProdutoTest {

    @Test
    void produtoNovoDeveComecarDisponivel() {
        assertTrue(new Produto().getDisponivel());
    }

    @Test
    void valoresDevemComecarNulos() {
        Produto produto = new Produto();

        assertNull(produto.getId());
        assertNull(produto.getValor());
        assertNull(produto.getValorCompra());
        assertNull(produto.getDescricao());
        assertNull(produto.getMarca());
    }

    @Test
    void gettersDevemDevolverOQueFoiDefinidoNosSetters() {
        Produto produto = new Produto();
        produto.setId(2L);
        produto.setCodigo("PRD-001");
        produto.setNome("Teclado");
        produto.setDescricao("Teclado mecanico");
        produto.setValor(new BigDecimal("199.90"));
        produto.setValorCompra(new BigDecimal("120.00"));
        produto.setDisponivel(false);
        produto.setMarca("Logitech");

        assertEquals(2L, produto.getId());
        assertEquals("PRD-001", produto.getCodigo());
        assertEquals("Teclado", produto.getNome());
        assertEquals("Teclado mecanico", produto.getDescricao());
        assertEquals(new BigDecimal("199.90"), produto.getValor());
        assertEquals(new BigDecimal("120.00"), produto.getValorCompra());
        assertEquals(false, produto.getDisponivel());
        assertEquals("Logitech", produto.getMarca());
    }

    @Test
    void produtoDeveSerPersistente() {
        assertInstanceOf(Persistente.class, new Produto());
    }
}
