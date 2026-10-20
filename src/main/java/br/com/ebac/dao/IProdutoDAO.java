package br.com.ebac.dao;

import br.com.ebac.dao.generic.IGenericDAO;
import br.com.ebac.domain.Produto;

/**
 * DAO de produto; a chave de consulta é o código.
 */
public interface IProdutoDAO extends IGenericDAO<Produto, String> {
}
