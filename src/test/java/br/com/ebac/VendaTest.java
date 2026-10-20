package br.com.ebac;

import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.Produto;
import br.com.ebac.domain.Venda;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class VendaTest {

    private Venda venda;
    private Produto arroz;
    private Produto feijao;

    @Before
    public void setUp() {
        venda = new Venda();
        arroz = criarProduto("ARROZ", "10.00");
        feijao = criarProduto("FEIJAO", "7.50");
    }

    private Produto criarProduto(String codigo, String valor) {
        Produto produto = new Produto();
        produto.setCodigo(codigo);
        produto.setNome(codigo);
        produto.setValor(new BigDecimal(valor));
        return produto;
    }

    @Test
    public void novaVendaDeveComecarVazia() {
        assertTrue(venda.getProdutos().isEmpty());
        assertEquals(0, venda.getValorTotal().compareTo(BigDecimal.ZERO));
        assertEquals(Integer.valueOf(0), venda.getQuantidadeTotalProdutos());
    }

    @Test
    public void deveGuardarOsDadosDaVenda() {
        Cliente cliente = new Cliente();
        LocalDateTime data = LocalDateTime.of(2026, 10, 20, 15, 30);

        venda.setId(1L);
        venda.setCodigo("V1");
        venda.setCliente(cliente);
        venda.setDataVenda(data);
        venda.setStatus(Venda.Status.INICIADA);

        assertEquals(Long.valueOf(1L), venda.getId());
        assertEquals("V1", venda.getCodigo());
        assertSame(cliente, venda.getCliente());
        assertEquals(data, venda.getDataVenda());
        assertEquals(Venda.Status.INICIADA, venda.getStatus());
    }

    @Test
    public void deveAdicionarProdutosECalcularTotal() {
        venda.adicionarProduto(arroz, 2);
        venda.adicionarProduto(feijao, 1);

        assertEquals(2, venda.getProdutos().size());
        assertEquals(Integer.valueOf(3), venda.getQuantidadeTotalProdutos());
        assertEquals(0, venda.getValorTotal().compareTo(new BigDecimal("27.50")));
    }

    @Test
    public void deveSomarQuantidadeAoAdicionarMesmoProduto() {
        venda.adicionarProduto(arroz, 2);
        venda.adicionarProduto(arroz, 3);

        assertEquals(1, venda.getProdutos().size());
        assertEquals(Integer.valueOf(5), venda.buscarItem("ARROZ").getQuantidade());
        assertEquals(0, venda.getValorTotal().compareTo(new BigDecimal("50.00")));
    }

    @Test
    public void deveRemoverParteDaQuantidade() {
        venda.adicionarProduto(arroz, 5);

        venda.removerProduto("ARROZ", 2);

        assertEquals(Integer.valueOf(3), venda.buscarItem("ARROZ").getQuantidade());
        assertEquals(0, venda.getValorTotal().compareTo(new BigDecimal("30.00")));
    }

    @Test
    public void deveTirarItemQuandoQuantidadeZerar() {
        venda.adicionarProduto(arroz, 2);
        venda.adicionarProduto(feijao, 1);

        venda.removerProduto("ARROZ", 2);

        assertNull(venda.buscarItem("ARROZ"));
        assertEquals(1, venda.getProdutos().size());
        assertEquals(0, venda.getValorTotal().compareTo(new BigDecimal("7.50")));
    }

    @Test
    public void removerProdutoInexistenteNaoAlteraVenda() {
        venda.adicionarProduto(arroz, 1);

        venda.removerProduto("FEIJAO", 1);

        assertEquals(1, venda.getProdutos().size());
        assertEquals(0, venda.getValorTotal().compareTo(new BigDecimal("10.00")));
    }
}
