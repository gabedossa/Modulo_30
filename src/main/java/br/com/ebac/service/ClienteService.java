package br.com.ebac.service;

import br.com.ebac.dao.ClienteDAO;
import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.domain.Cliente;

import java.sql.SQLException;
import java.util.List;

public class ClienteService implements IClienteService {

    private final IClienteDAO dao;

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(IClienteDAO dao) {
        this.dao = dao;
    }

    @Override
    public Cliente cadastrar(Cliente cliente) throws SQLException {
        validar(cliente);
        if (dao.buscarPorCpf(cliente.getCpf()) != null) {
            throw new IllegalArgumentException("Já existe cliente com o CPF " + cliente.getCpf());
        }
        dao.cadastrar(cliente);
        return cliente;
    }

    @Override
    public Cliente buscarPorCpf(String cpf) throws SQLException {
        return dao.buscarPorCpf(normalizarCpf(cpf));
    }

    @Override
    public List<Cliente> buscarTodos() throws SQLException {
        return dao.buscarTodos();
    }

    @Override
    public Cliente atualizar(Cliente cliente) throws SQLException {
        validar(cliente);
        if (cliente.getId() == null) {
            throw new IllegalArgumentException("Cliente sem id não pode ser atualizado");
        }
        Cliente existente = dao.buscarPorCpf(cliente.getCpf());
        if (existente != null && !existente.getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Já existe outro cliente com o CPF " + cliente.getCpf());
        }
        if (dao.atualizar(cliente) == 0) {
            throw new IllegalArgumentException("Cliente com id " + cliente.getId() + " não encontrado");
        }
        return cliente;
    }

    @Override
    public void excluir(String cpf) throws SQLException {
        Cliente cliente = buscarPorCpf(cpf);
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente com CPF " + cpf + " não encontrado");
        }
        dao.excluir(cliente);
    }

    private void validar(Cliente cliente) {
        if (cliente == null) {
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        }
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        cliente.setCpf(normalizarCpf(cliente.getCpf()));
        if (cliente.getCpf().length() != 11) {
            throw new IllegalArgumentException("CPF deve ter 11 dígitos");
        }
        if (cliente.getEstado() != null) {
            cliente.setEstado(cliente.getEstado().trim().toUpperCase());
            if (!cliente.getEstado().matches("[A-Z]{2}")) {
                throw new IllegalArgumentException("Estado deve ser a sigla com 2 letras");
            }
        }
        if (cliente.getCasaNumero() != null && cliente.getCasaNumero() <= 0) {
            throw new IllegalArgumentException("Número da casa deve ser positivo");
        }
    }

    private String normalizarCpf(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }
}
