package br.com.ebac.dao;

import br.com.ebac.dao.generic.IGenericDAO;
import br.com.ebac.domain.Venda;

/**
 * DAO de venda; a chave de consulta é o código.
 */
public interface IVendaDAO extends IGenericDAO<Venda, String> {
}
