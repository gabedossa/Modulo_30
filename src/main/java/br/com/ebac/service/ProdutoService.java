package br.com.ebac.service;

import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.dao.ProdutoDAO;
import br.com.ebac.domain.Produto;
import br.com.ebac.service.generic.GenericService;

import java.math.BigDecimal;

public class ProdutoService extends GenericService<Produto, String> implements IProdutoService {

    public ProdutoService() {
        this(new ProdutoDAO());
    }

    public ProdutoService(IProdutoDAO dao) {
        super(dao);
    }

    @Override
    protected void validar(Produto produto) {
        produto.setCodigo(normalizarChave(produto.getCodigo()));
        if (produto.getCodigo().isEmpty()) {
            throw new IllegalArgumentException("Código é obrigatório");
        }
        if (produto.getNome() == null || produto.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (produto.getValor() == null || produto.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor deve ser maior que zero");
        }
    }

    @Override
    protected String normalizarChave(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    @Override
    protected String getDescricao() {
        return "produto com o código";
    }
}
