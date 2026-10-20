package br.com.ebac.dao.generic;

import br.com.ebac.dao.jdbc.ConnectionFactory;
import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação JDBC do CRUD. As subclasses só informam a tabela, as colunas
 * e como converter a entidade de/para o banco.
 */
public abstract class GenericDAO<T extends Persistente, E extends Serializable> implements IGenericDAO<T, E> {

    protected abstract String getTabela();

    /** Coluna usada em {@link #consultar}, ex.: cpf ou codigo. */
    protected abstract String getColunaChave();

    /** Colunas gravadas no insert e no update, sem o id e na mesma ordem de {@link #preencherParametros}. */
    protected abstract String[] getColunas();

    protected abstract void preencherParametros(PreparedStatement stm, T entidade) throws SQLException;

    protected abstract T converter(Connection connection, ResultSet rs) throws SQLException;

    /** Executado na mesma transação, logo depois do insert. */
    protected void depoisDeCadastrar(Connection connection, T entidade) throws SQLException {
    }

    /** Executado na mesma transação, logo depois do update. */
    protected void depoisDeAtualizar(Connection connection, T entidade) throws SQLException {
    }

    @Override
    public Integer cadastrar(T entidade) throws SQLException {
        String[] colunas = getColunas();
        String sql = "INSERT INTO " + getTabela() + " (" + String.join(", ", colunas) + ") VALUES ("
                + String.join(", ", Collections.nCopies(colunas.length, "?")) + ")";
        return emTransacao(connection -> {
            int linhas;
            try (PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preencherParametros(stm, entidade);
                linhas = stm.executeUpdate();
                try (ResultSet rs = stm.getGeneratedKeys()) {
                    if (rs.next()) {
                        entidade.setId(rs.getLong("id"));
                    }
                }
            }
            depoisDeCadastrar(connection, entidade);
            return linhas;
        });
    }

    @Override
    public T consultar(E chave) throws SQLException {
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + getColunaChave() + " = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setObject(1, chave);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? converter(connection, rs) : null;
            }
        }
    }

    @Override
    public List<T> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM " + getTabela() + " ORDER BY id";
        List<T> entidades = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                entidades.add(converter(connection, rs));
            }
        }
        return entidades;
    }

    @Override
    public Integer atualizar(T entidade) throws SQLException {
        String[] colunas = getColunas();
        String sql = "UPDATE " + getTabela() + " SET " + String.join(" = ?, ", colunas) + " = ? WHERE id = ?";
        return emTransacao(connection -> {
            int linhas;
            try (PreparedStatement stm = connection.prepareStatement(sql)) {
                preencherParametros(stm, entidade);
                stm.setLong(colunas.length + 1, entidade.getId());
                linhas = stm.executeUpdate();
            }
            depoisDeAtualizar(connection, entidade);
            return linhas;
        });
    }

    @Override
    public Integer excluir(T entidade) throws SQLException {
        String sql = "DELETE FROM " + getTabela() + " WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, entidade.getId());
            return stm.executeUpdate();
        }
    }

    private Integer emTransacao(OperacaoSql operacao) throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                Integer resultado = operacao.executar(connection);
                connection.commit();
                return resultado;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
    }

    private interface OperacaoSql {
        Integer executar(Connection connection) throws SQLException;
    }
}
