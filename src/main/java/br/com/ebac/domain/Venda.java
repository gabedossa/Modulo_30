package br.com.ebac.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Venda implements Persistente {

    public enum Status {
        INICIADA, CONCLUIDA, CANCELADA
    }

    private Long id;
    private String codigo;
    private Cliente cliente;
    private List<ProdutoQuantidade> produtos = new ArrayList<>();
    private BigDecimal valorTotal = BigDecimal.ZERO;
    private LocalDateTime dataVenda;
    private Status status;

    public void adicionarProduto(Produto produto, Integer quantidade) {
        ProdutoQuantidade item = buscarItem(produto.getCodigo());
        if (item == null) {
            item = new ProdutoQuantidade(produto);
            produtos.add(item);
        }
        item.adicionar(quantidade);
        recalcularValorTotal();
    }

    public void removerProduto(String codigoProduto, Integer quantidade) {
        ProdutoQuantidade item = buscarItem(codigoProduto);
        if (item == null) {
            return;
        }
        item.remover(quantidade);
        if (item.getQuantidade() <= 0) {
            produtos.remove(item);
        }
        recalcularValorTotal();
    }

    public ProdutoQuantidade buscarItem(String codigoProduto) {
        for (ProdutoQuantidade item : produtos) {
            if (item.getProduto().getCodigo().equals(codigoProduto)) {
                return item;
            }
        }
        return null;
    }

    public Integer getQuantidadeTotalProdutos() {
        int total = 0;
        for (ProdutoQuantidade item : produtos) {
            total += item.getQuantidade();
        }
        return total;
    }

    public void recalcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ProdutoQuantidade item : produtos) {
            total = total.add(item.getValorTotal());
        }
        this.valorTotal = total;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<ProdutoQuantidade> getProdutos() {
        return produtos;
    }

    public void setProdutos(List<ProdutoQuantidade> produtos) {
        this.produtos = produtos;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public LocalDateTime getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(LocalDateTime dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
