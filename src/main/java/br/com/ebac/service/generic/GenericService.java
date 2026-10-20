package br.com.ebac.service.generic;

import br.com.ebac.dao.generic.IGenericDAO;
import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

/**
 * Regras comuns de cadastro: entidade não nula, validação específica,
 * chave sem duplicidade e existência antes de atualizar ou excluir.
 */
public abstract class GenericService<T extends Persistente, E extends Serializable> implements IGenericService<T, E> {

    protected final IGenericDAO<T, E> dao;

    protected GenericService(IGenericDAO<T, E> dao) {
        this.dao = dao;
    }

    /** Valida e normaliza a entidade; lança IllegalArgumentException se algo estiver errado. */
    protected abstract void validar(T entidade) throws SQLException;

    protected abstract E getChave(T entidade);

    protected abstract E normalizarChave(E chave);

    /** Usada nas mensagens de erro, ex.: "cliente com o CPF". */
    protected abstract String getDescricao();

    @Override
    public T cadastrar(T entidade) throws SQLException {
        validarNaoNulo(entidade);
        validar(entidade);
        if (dao.consultar(getChave(entidade)) != null) {
            throw new IllegalArgumentException("Já existe " + getDescricao() + " " + getChave(entidade));
        }
        dao.cadastrar(entidade);
        return entidade;
    }

    @Override
    public T consultar(E chave) throws SQLException {
        return dao.consultar(normalizarChave(chave));
    }

    @Override
    public List<T> buscarTodos() throws SQLException {
        return dao.buscarTodos();
    }

    @Override
    public T atualizar(T entidade) throws SQLException {
        validarNaoNulo(entidade);
        validar(entidade);
        if (entidade.getId() == null) {
            throw new IllegalArgumentException("Registro sem id não pode ser atualizado");
        }
        T existente = dao.consultar(getChave(entidade));
        if (existente != null && !existente.getId().equals(entidade.getId())) {
            throw new IllegalArgumentException("Já existe outro registro: " + getDescricao() + " " + getChave(entidade));
        }
        if (dao.atualizar(entidade) == 0) {
            throw new IllegalArgumentException("Registro com id " + entidade.getId() + " não encontrado");
        }
        return entidade;
    }

    @Override
    public void excluir(E chave) throws SQLException {
        dao.excluir(consultarExistente(chave));
    }

    protected T consultarExistente(E chave) throws SQLException {
        T entidade = consultar(chave);
        if (entidade == null) {
            throw new IllegalArgumentException("Registro não encontrado: " + getDescricao() + " " + chave);
        }
        return entidade;
    }

    private void validarNaoNulo(T entidade) {
        if (entidade == null) {
            throw new IllegalArgumentException("Registro não pode ser nulo");
        }
    }
}
