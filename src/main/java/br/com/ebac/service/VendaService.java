package br.com.ebac.service;

import br.com.ebac.dao.ClienteDAO;
import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.dao.IVendaDAO;
import br.com.ebac.dao.ProdutoDAO;
import br.com.ebac.dao.VendaDAO;
import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.Produto;
import br.com.ebac.domain.ProdutoQuantidade;
import br.com.ebac.domain.Venda;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaService implements IVendaService {

    private final IVendaDAO vendaDAO;
    private final IClienteDAO clienteDAO;
    private final IProdutoDAO produtoDAO;

    public VendaService() {
        this(new VendaDAO(), new ClienteDAO(), new ProdutoDAO());
    }

    public VendaService(IVendaDAO vendaDAO, IClienteDAO clienteDAO, IProdutoDAO produtoDAO) {
        this.vendaDAO = vendaDAO;
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
    }

    @Override
    public Venda cadastrar(Venda venda) throws SQLException {
        if (venda == null) {
            throw new IllegalArgumentException("Venda não pode ser nula");
        }
        venda.setCodigo(normalizarCodigo(venda.getCodigo()));
        if (venda.getCodigo().isEmpty()) {
            throw new IllegalArgumentException("Código da venda é obrigatório");
        }
        if (vendaDAO.buscarPorCodigo(venda.getCodigo()) != null) {
            throw new IllegalArgumentException("Já existe venda com o código " + venda.getCodigo());
        }
        venda.setCliente(buscarCliente(venda.getCliente()));

        // recria os itens com os produtos do banco, para usar o id e o valor atuais
        List<ProdutoQuantidade> itens = venda.getProdutos();
        venda.setProdutos(new ArrayList<>());
        for (ProdutoQuantidade item : itens) {
            validarQuantidade(item.getQuantidade());
            venda.adicionarProduto(buscarProduto(item.getProduto().getCodigo()), item.getQuantidade());
        }
        venda.recalcularValorTotal();

        venda.setStatus(Venda.Status.INICIADA);
        if (venda.getDataVenda() == null) {
            venda.setDataVenda(LocalDateTime.now());
        }
        vendaDAO.cadastrar(venda);
        return venda;
    }

    @Override
    public Venda buscarPorCodigo(String codigo) throws SQLException {
        return vendaDAO.buscarPorCodigo(normalizarCodigo(codigo));
    }

    @Override
    public List<Venda> buscarTodos() throws SQLException {
        return vendaDAO.buscarTodos();
    }

    @Override
    public Venda adicionarProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException {
        Venda venda = buscarVendaIniciada(codigoVenda);
        validarQuantidade(quantidade);
        venda.adicionarProduto(buscarProduto(codigoProduto), quantidade);
        vendaDAO.atualizar(venda);
        return venda;
    }

    @Override
    public Venda removerProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException {
        Venda venda = buscarVendaIniciada(codigoVenda);
        validarQuantidade(quantidade);
        ProdutoQuantidade item = venda.buscarItem(normalizarCodigo(codigoProduto));
        if (item == null) {
            throw new IllegalArgumentException("Produto " + codigoProduto + " não está na venda");
        }
        if (quantidade > item.getQuantidade()) {
            throw new IllegalArgumentException("Quantidade maior que a existente na venda");
        }
        venda.removerProduto(item.getProduto().getCodigo(), quantidade);
        vendaDAO.atualizar(venda);
        return venda;
    }

    @Override
    public Venda finalizar(String codigoVenda) throws SQLException {
        Venda venda = buscarVendaIniciada(codigoVenda);
        if (venda.getProdutos().isEmpty()) {
            throw new IllegalArgumentException("Venda sem produtos não pode ser finalizada");
        }
        venda.setStatus(Venda.Status.CONCLUIDA);
        vendaDAO.atualizar(venda);
        return venda;
    }

    @Override
    public Venda cancelar(String codigoVenda) throws SQLException {
        Venda venda = buscarVendaExistente(codigoVenda);
        if (venda.getStatus() == Venda.Status.CANCELADA) {
            throw new IllegalArgumentException("Venda " + venda.getCodigo() + " já está cancelada");
        }
        venda.setStatus(Venda.Status.CANCELADA);
        vendaDAO.atualizar(venda);
        return venda;
    }

    @Override
    public void excluir(String codigoVenda) throws SQLException {
        vendaDAO.excluir(buscarVendaExistente(codigoVenda));
    }

    private Venda buscarVendaExistente(String codigoVenda) throws SQLException {
        Venda venda = buscarPorCodigo(codigoVenda);
        if (venda == null) {
            throw new IllegalArgumentException("Venda com código " + codigoVenda + " não encontrada");
        }
        return venda;
    }

    private Venda buscarVendaIniciada(String codigoVenda) throws SQLException {
        Venda venda = buscarVendaExistente(codigoVenda);
        if (venda.getStatus() != Venda.Status.INICIADA) {
            throw new IllegalArgumentException("Venda " + venda.getCodigo() + " não pode ser alterada, status: "
                    + venda.getStatus());
        }
        return venda;
    }

    private Cliente buscarCliente(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getCpf() == null) {
            throw new IllegalArgumentException("Cliente é obrigatório");
        }
        Cliente cadastrado = clienteDAO.buscarPorCpf(cliente.getCpf().replaceAll("\\D", ""));
        if (cadastrado == null) {
            throw new IllegalArgumentException("Cliente com CPF " + cliente.getCpf() + " não cadastrado");
        }
        return cadastrado;
    }

    private Produto buscarProduto(String codigoProduto) throws SQLException {
        Produto produto = produtoDAO.buscarPorCodigo(normalizarCodigo(codigoProduto));
        if (produto == null) {
            throw new IllegalArgumentException("Produto com código " + codigoProduto + " não cadastrado");
        }
        return produto;
    }

    private void validarQuantidade(Integer quantidade) {
        if (quantidade == null || quantidade <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
    }

    private String normalizarCodigo(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }
}
