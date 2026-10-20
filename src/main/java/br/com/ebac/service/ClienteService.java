package br.com.ebac.service;

import br.com.ebac.dao.ClienteDAO;
import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.domain.Cliente;
import br.com.ebac.service.generic.GenericService;

public class ClienteService extends GenericService<Cliente, String> implements IClienteService {

    public ClienteService() {
        this(new ClienteDAO());
    }

    public ClienteService(IClienteDAO dao) {
        super(dao);
    }

    @Override
    protected void validar(Cliente cliente) {
        if (cliente.getNome() == null || cliente.getNome().trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        cliente.setCpf(normalizarChave(cliente.getCpf()));
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

    @Override
    protected String normalizarChave(String cpf) {
        return cpf == null ? "" : cpf.replaceAll("\\D", "");
    }

    @Override
    protected String getDescricao() {
        return "cliente com o CPF";
    }
}
