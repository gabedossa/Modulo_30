package model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

class ProdutoQuantidadeTest {

    private ProdutoQuantidade novoItem(String valorDoProduto) {
        Produto produto = new Produto();
        produto.setValor(new BigDecimal(valorDoProduto));

        ProdutoQuantidade item = new ProdutoQuantidade();
        item.setProduto(produto);
        return item;
    }

    @Test
    void deveComecarComQuantidadeEValorTotalZerados() {
        ProdutoQuantidade item = new ProdutoQuantidade();

        assertEquals(0, item.getQuantidade());
        assertEquals(BigDecimal.ZERO, item.getValorTotal());
    }

    @Test
    void adicionarDeveSomarQuantidadeEValorTotal() {
        ProdutoQuantidade item = novoItem("10.50");

        item.adicionar(2);
        item.adicionar(3);

        assertEquals(5, item.getQuantidade());
        assertEquals(0, new BigDecimal("52.50").compareTo(item.getValorTotal()));
    }

    @Test
    void removerDeveSubtrairQuantidadeEValorTotal() {
        ProdutoQuantidade item = novoItem("10.50");
        item.adicionar(5);

        item.remover(2);

        assertEquals(3, item.getQuantidade());
        assertEquals(0, new BigDecimal("31.50").compareTo(item.getValorTotal()));
    }
}
