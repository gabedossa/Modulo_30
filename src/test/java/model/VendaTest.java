package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class VendaTest {

    private Produto produto(String codigo, String valor) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setValor(new BigDecimal(valor));
        return produto;
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual), "esperado " + esperado + ", veio " + atual);
    }

    @Test
    void vendaNovaDeveComecarSemProdutos() {
        Venda venda = new Venda();

        assertTrue(venda.getProdutos().isEmpty());
        assertEquals(0, venda.getQuantidadeTotalProdutos());
    }

    @Test
    void adicionarProdutoDeveCalcularValorTotal() {
        Venda venda = new Venda();

        venda.adicionarProduto(produto("A", "10.00"), 2);
        venda.adicionarProduto(produto("B", "5.50"), 1);

        assertEquals(2, venda.getProdutos().size());
        assertEquals(3, venda.getQuantidadeTotalProdutos());
        assertValor("25.50", venda.getValorTotal());
    }

    @Test
    void adicionarMesmoProdutoDeveSomarNoMesmoItem() {
        Venda venda = new Venda();

        venda.adicionarProduto(produto("A", "10.00"), 2);
        venda.adicionarProduto(produto("A", "10.00"), 3);

        assertEquals(1, venda.getProdutos().size());
        assertEquals(5, venda.getQuantidadeTotalProdutos());
        assertValor("50.00", venda.getValorTotal());
    }

    @Test
    void removerParteDoProdutoDeveManterOItem() {
        Venda venda = new Venda();
        venda.adicionarProduto(produto("A", "10.00"), 5);

        venda.removerProduto(produto("A", "10.00"), 2);

        assertEquals(1, venda.getProdutos().size());
        assertEquals(3, venda.getQuantidadeTotalProdutos());
        assertValor("30.00", venda.getValorTotal());
    }

    @Test
    void removerTodaQuantidadeDeveTirarOItemDaVenda() {
        Venda venda = new Venda();
        venda.adicionarProduto(produto("A", "10.00"), 2);
        venda.adicionarProduto(produto("B", "5.00"), 1);

        venda.removerProduto(produto("A", "10.00"), 2);

        assertEquals(1, venda.getProdutos().size());
        assertValor("5.00", venda.getValorTotal());
    }

    @Test
    void removerTodosProdutosDeveZerarAVenda() {
        Venda venda = new Venda();
        venda.adicionarProduto(produto("A", "10.00"), 2);

        venda.removerTodosProdutos();

        assertTrue(venda.getProdutos().isEmpty());
        assertValor("0", venda.getValorTotal());
    }

    @Test
    void vendaConcluidaNaoPodeSerAlterada() {
        Venda venda = new Venda();
        venda.adicionarProduto(produto("A", "10.00"), 1);
        venda.setStatus(Venda.Status.CONCLUIDA);

        assertThrows(UnsupportedOperationException.class, () -> venda.adicionarProduto(produto("B", "1.00"), 1));
        assertThrows(UnsupportedOperationException.class, () -> venda.removerProduto(produto("A", "10.00"), 1));
        assertThrows(UnsupportedOperationException.class, venda::removerTodosProdutos);
    }

    @Test
    void statusGetByNameDeveConverterTexto() {
        assertEquals(Venda.Status.INICIADA, Venda.Status.getByName("INICIADA"));
        assertEquals(Venda.Status.CONCLUIDA, Venda.Status.getByName("CONCLUIDA"));
        assertEquals(Venda.Status.CANCELADA, Venda.Status.getByName("CANCELADA"));
        assertNull(Venda.Status.getByName("INEXISTENTE"));
    }
}
