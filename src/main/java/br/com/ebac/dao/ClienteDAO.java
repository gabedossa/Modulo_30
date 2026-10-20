package br.com.ebac.dao;

import br.com.ebac.dao.generic.GenericDAO;
import br.com.ebac.domain.Cliente;

/**
 * Tabela, colunas e chave (CPF) vêm das annotations de {@link Cliente}.
 */
public class ClienteDAO extends GenericDAO<Cliente, String> implements IClienteDAO {

    @Override
    public Class<Cliente> getTipoClasse() {
        return Cliente.class;
    }
}
