package br.com.ebac.dao.generic;

import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * Operações de CRUD comuns a todos os DAOs.
 *
 * @param <T> entidade persistida
 * @param <E> tipo da chave de negócio usada em {@link #consultar}, ex.: o CPF do cliente
 */
public interface IGenericDAO<T extends Persistente, E extends Serializable> {

    Integer cadastrar(T entidade) throws SQLException;

    T consultar(E chave) throws SQLException;

    List<T> buscarTodos() throws SQLException;

    Integer atualizar(T entidade) throws SQLException;

    Integer excluir(T entidade) throws SQLException;
}
