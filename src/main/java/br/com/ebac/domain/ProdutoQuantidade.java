package br.com.ebac.domain;

import java.math.BigDecimal;

public class ProdutoQuantidade {

    private Produto produto;
    private Integer quantidade;
    private BigDecimal valorTotal;

    public ProdutoQuantidade() {
        this.quantidade = 0;
        this.valorTotal = BigDecimal.ZERO;
    }

    public ProdutoQuantidade(Produto produto) {
        this();
        this.produto = produto;
    }

    public void adicionar(Integer quantidade) {
        this.quantidade += quantidade;
        recalcularValorTotal();
    }

    public void remover(Integer quantidade) {
        this.quantidade -= quantidade;
        recalcularValorTotal();
    }

    private void recalcularValorTotal() {
        this.valorTotal = produto.getValor().multiply(BigDecimal.valueOf(quantidade));
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }
}
