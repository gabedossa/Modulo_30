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
import br.com.ebac.service.generic.GenericService;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VendaService extends GenericService<Venda, String> implements IVendaService {

    private final IClienteDAO clienteDAO;
    private final IProdutoDAO produtoDAO;

    public VendaService() {
        this(new VendaDAO(), new ClienteDAO(), new ProdutoDAO());
    }

    public VendaService(IVendaDAO vendaDAO, IClienteDAO clienteDAO, IProdutoDAO produtoDAO) {
        super(vendaDAO);
        this.clienteDAO = clienteDAO;
        this.produtoDAO = produtoDAO;
    }

    @Override
    public Venda cadastrar(Venda venda) throws SQLException {
        if (venda != null) {
            venda.setStatus(Venda.Status.INICIADA);
            if (venda.getDataVenda() == null) {
                venda.setDataVenda(LocalDateTime.now());
            }
        }
        return super.cadastrar(venda);
    }

    /** Só vendas iniciadas podem ser alteradas; o status muda apenas por finalizar e cancelar. */
    @Override
    public Venda atualizar(Venda venda) throws SQLException {
        if (venda != null) {
            Venda salva = dao.consultar(normalizarChave(venda.getCodigo()));
            if (salva != null) {
                validarIniciada(salva);
            }
            venda.setStatus(Venda.Status.INICIADA);
        }
        return super.atualizar(venda);
    }

    @Override
    public Venda adicionarProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException {
        Venda venda = consultarIniciada(codigoVenda);
        validarQuantidade(quantidade);
        venda.adicionarProduto(buscarProduto(codigoProduto), quantidade);
        dao.atualizar(venda);
        return venda;
    }

    @Override
    public Venda removerProduto(String codigoVenda, String codigoProduto, Integer quantidade) throws SQLException {
        Venda venda = consultarIniciada(codigoVenda);
        validarQuantidade(quantidade);
        ProdutoQuantidade item = venda.buscarItem(normalizarChave(codigoProduto));
        if (item == null) {
            throw new IllegalArgumentException("Produto " + codigoProduto + " não está na venda");
        }
        if (quantidade > item.getQuantidade()) {
            throw new IllegalArgumentException("Quantidade maior que a existente na venda");
        }
        venda.removerProduto(item.getProduto().getCodigo(), quantidade);
        dao.atualizar(venda);
        return venda;
    }

    @Override
    public Venda finalizar(String codigoVenda) throws SQLException {
        Venda venda = consultarIniciada(codigoVenda);
        if (venda.getProdutos().isEmpty()) {
            throw new IllegalArgumentException("Venda sem produtos não pode ser finalizada");
        }
        venda.setStatus(Venda.Status.CONCLUIDA);
        dao.atualizar(venda);
        return venda;
    }

    @Override
    public Venda cancelar(String codigoVenda) throws SQLException {
        Venda venda = consultarExistente(codigoVenda);
        if (venda.getStatus() == Venda.Status.CANCELADA) {
            throw new IllegalArgumentException("Venda " + venda.getCodigo() + " já está cancelada");
        }
        venda.setStatus(Venda.Status.CANCELADA);
        dao.atualizar(venda);
        return venda;
    }

    @Override
    protected void validar(Venda venda) throws SQLException {
        venda.setCodigo(normalizarChave(venda.getCodigo()));
        if (venda.getCodigo().isEmpty()) {
            throw new IllegalArgumentException("Código da venda é obrigatório");
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
    }

    @Override
    protected String normalizarChave(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase();
    }

    @Override
    protected String getDescricao() {
        return "venda com o código";
    }

    private Venda consultarIniciada(String codigoVenda) throws SQLException {
        Venda venda = consultarExistente(codigoVenda);
        validarIniciada(venda);
        return venda;
    }

    private void validarIniciada(Venda venda) {
        if (venda.getStatus() != Venda.Status.INICIADA) {
            throw new IllegalArgumentException("Venda " + venda.getCodigo() + " não pode ser alterada, status: "
                    + venda.getStatus());
        }
    }

    private Cliente buscarCliente(Cliente cliente) throws SQLException {
        if (cliente == null || cliente.getCpf() == null) {
            throw new IllegalArgumentException("Cliente é obrigatório");
        }
        Cliente cadastrado = clienteDAO.consultar(cliente.getCpf().replaceAll("\\D", ""));
        if (cadastrado == null) {
            throw new IllegalArgumentException("Cliente com CPF " + cliente.getCpf() + " não cadastrado");
        }
        return cadastrado;
    }

    private Produto buscarProduto(String codigoProduto) throws SQLException {
        Produto produto = produtoDAO.consultar(normalizarChave(codigoProduto));
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
}
