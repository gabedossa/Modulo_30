package br.com.ebac.dao;

import br.com.ebac.dao.generic.IGenericDAO;
import br.com.ebac.domain.Cliente;

/**
 * DAO de cliente; a chave de consulta é o CPF.
 */
public interface IClienteDAO extends IGenericDAO<Cliente, String> {
}
