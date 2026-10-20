package br.com.ebac;

import br.com.ebac.dao.generic.IGenericDAO;
import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * DAO em memória para testar os services sem depender do banco.
 */
public class GenericDAOMock<T extends Persistente, E extends Serializable> implements IGenericDAO<T, E> {

    private final Map<Long, T> registros = new LinkedHashMap<>();
    private final Function<T, E> chave;
    private long proximoId = 1;

    public GenericDAOMock(Function<T, E> chave) {
        this.chave = chave;
    }

    @Override
    public Integer cadastrar(T entidade) {
        entidade.setId(proximoId++);
        registros.put(entidade.getId(), entidade);
        return 1;
    }

    @Override
    public T consultar(E valor) {
        return registros.values().stream()
                .filter(r -> chave.apply(r).equals(valor))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<T> buscarTodos() {
        return new ArrayList<>(registros.values());
    }

    @Override
    public Integer atualizar(T entidade) {
        if (!registros.containsKey(entidade.getId())) {
            return 0;
        }
        registros.put(entidade.getId(), entidade);
        return 1;
    }

    @Override
    public Integer excluir(T entidade) {
        return registros.remove(entidade.getId()) != null ? 1 : 0;
    }
}
