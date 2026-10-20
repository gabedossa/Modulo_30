package br.com.ebac;

import br.com.ebac.dao.IClienteDAO;
import br.com.ebac.domain.Cliente;

public class ClienteDAOMock extends GenericDAOMock<Cliente, String> implements IClienteDAO {

    public ClienteDAOMock() {
        super(Cliente::getCpf);
    }
}
