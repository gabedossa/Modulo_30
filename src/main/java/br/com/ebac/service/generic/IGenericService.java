package br.com.ebac.service.generic;

import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * Operações comuns a todos os services.
 *
 * @param <T> entidade
 * @param <E> tipo da chave de negócio, ex.: o CPF do cliente
 */
public interface IGenericService<T extends Persistente, E extends Serializable> {

    T cadastrar(T entidade) throws SQLException;

    T consultar(E chave) throws SQLException;

    List<T> buscarTodos() throws SQLException;

    T atualizar(T entidade) throws SQLException;

    void excluir(E chave) throws SQLException;
}
