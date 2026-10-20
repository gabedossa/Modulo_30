package br.com.ebac.service;

import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.dao.ProdutoDAO;
import br.com.ebac.domain.Produto;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProdutoService implements IProdutoService {

    private final IProdutoDAO dao;

    public ProdutoService() {
        this(new ProdutoDAO());
    }

    public ProdutoService(IProdutoDAO dao) {
        this.dao = dao;
    }

    @Override
    public Produto cadastrar(Produto produto) throws SQLException {
        validar(produto);
        if (dao.buscarPorCodigo(produto.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe produto com o código " + produto.getCodigo());
        }
        dao.cadastrar(produto);
        return produto;
    }

    @Override
    public Produto buscarPorCodigo(String codigo) throws SQLException {
        return dao.buscarPorCodigo(normalizarCodigo(codigo));
    }

    @Override
    public List<Produto> buscarTodos() throws SQLException {
        return dao.buscarTodos();
    }

    @Override
    public Produto atualizar(Produto produto) throws SQLException {
        validar(produto);
        if (produto.getId() == null) {
            throw new IllegalArgumentException("Produto sem id não pode ser atualizado");
        }
        Produto existente = dao.buscarPorCodigo(produto.getCodigo());
        if (existente != null && !existente.getId().equals(produto.getId())) {
            throw new IllegalArgumentException("Já existe outro produto com o código " + produto.getCodigo());
        }
        if (dao.atualizar(produto) == 0) {
            throw new IllegalArgumentException("Produto com id " + produto.getId() + " não encontrado");
        }
        return produto;
    }

    @Override
    public void excluir(String codigo) throws SQLException {
        Produto produto = buscarPorCodigo(codigo);
        if (produto == null) {
            throw new IllegalArgumentException("Produto com código " + codigo + " não encontrado");
        }
        dao.excluir(produto);
    }

    private void validar(Produto produto) {
        if (produto == null) {
            throw new IllegalArgumentException("Produto não pode ser nulo");
        }
        produto.setCodigo(normalizarCodigo(produto.getCodigo()));
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

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
