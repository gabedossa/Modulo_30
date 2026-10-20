package br.com.ebac.dao.generic;

import br.com.ebac.anotacao.AnotacaoUtil;
import br.com.ebac.dao.jdbc.ConnectionFactory;
import br.com.ebac.domain.Persistente;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implementação JDBC do CRUD. A tabela, as colunas e a chave são lidas das annotations
 * {@code @Tabela}, {@code @ColunaTabela} e {@code @TipoChave} da entidade.
 *
 * <p>Atributos que apontam para outra entidade (ex.: o cliente da venda) são gravados pelo id;
 * na leitura, a subclasse é quem preenche esses atributos.</p>
 */
public abstract class GenericDAO<T extends Persistente, E extends Serializable> implements IGenericDAO<T, E> {

    private static final String COLUNA_ID = "id";

    public abstract Class<T> getTipoClasse();

    /** Executado na mesma transação, logo depois do insert. */
    protected void depoisDeCadastrar(Connection connection, T entidade) throws SQLException {
    }

    /** Executado na mesma transação, logo depois do update. */
    protected void depoisDeAtualizar(Connection connection, T entidade) throws SQLException {
    }

    @Override
    public Integer cadastrar(T entidade) throws SQLException {
        List<Field> campos = getCamposGravaveis();
        String sql = "INSERT INTO " + getTabela() + " (" + String.join(", ", getColunas(campos)) + ") VALUES ("
                + String.join(", ", Collections.nCopies(campos.size(), "?")) + ")";
        return emTransacao(connection -> {
            int linhas;
            try (PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                preencherParametros(stm, campos, entidade);
                linhas = stm.executeUpdate();
                try (ResultSet rs = stm.getGeneratedKeys()) {
                    if (rs.next()) {
                        entidade.setId(rs.getLong(COLUNA_ID));
                    }
                }
            }
            depoisDeCadastrar(connection, entidade);
            return linhas;
        });
    }

    @Override
    public T consultar(E chave) throws SQLException {
        String coluna = AnotacaoUtil.getColuna(AnotacaoUtil.getCampoChave(getTipoClasse()));
        String sql = "SELECT * FROM " + getTabela() + " WHERE " + coluna + " = ?";
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
        String sql = "SELECT * FROM " + getTabela() + " ORDER BY " + COLUNA_ID;
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
        List<Field> campos = getCamposGravaveis();
        String sql = "UPDATE " + getTabela() + " SET " + String.join(" = ?, ", getColunas(campos))
                + " = ? WHERE " + COLUNA_ID + " = ?";
        return emTransacao(connection -> {
            int linhas;
            try (PreparedStatement stm = connection.prepareStatement(sql)) {
                preencherParametros(stm, campos, entidade);
                stm.setLong(campos.size() + 1, entidade.getId());
                linhas = stm.executeUpdate();
            }
            depoisDeAtualizar(connection, entidade);
            return linhas;
        });
    }

    @Override
    public Integer excluir(T entidade) throws SQLException {
        String sql = "DELETE FROM " + getTabela() + " WHERE " + COLUNA_ID + " = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, entidade.getId());
            return stm.executeUpdate();
        }
    }

    /**
     * Monta a entidade a partir da linha atual do ResultSet. Público para que um DAO
     * possa converter as entidades relacionadas, como o VendaDAO faz com o cliente.
     */
    public T converter(Connection connection, ResultSet rs) throws SQLException {
        T entidade = novaInstancia();
        for (Field campo : AnotacaoUtil.getCampos(getTipoClasse())) {
            if (!Persistente.class.isAssignableFrom(campo.getType())) {
                AnotacaoUtil.setValor(campo, entidade, lerValor(rs, AnotacaoUtil.getColuna(campo), campo.getType()));
            }
        }
        return entidade;
    }

    private String getTabela() {
        return AnotacaoUtil.getTabela(getTipoClasse());
    }

    /** Todos os campos mapeados, menos o id, que é gerado pelo banco. */
    private List<Field> getCamposGravaveis() {
        List<Field> campos = new ArrayList<>();
        for (Field campo : AnotacaoUtil.getCampos(getTipoClasse())) {
            if (!COLUNA_ID.equals(AnotacaoUtil.getColuna(campo))) {
                campos.add(campo);
            }
        }
        return campos;
    }

    private List<String> getColunas(List<Field> campos) {
        List<String> colunas = new ArrayList<>();
        for (Field campo : campos) {
            colunas.add(AnotacaoUtil.getColuna(campo));
        }
        return colunas;
    }

    private void preencherParametros(PreparedStatement stm, List<Field> campos, T entidade) throws SQLException {
        for (int i = 0; i < campos.size(); i++) {
            stm.setObject(i + 1, paraBanco(AnotacaoUtil.getValor(campos.get(i), entidade)));
        }
    }

    private Object paraBanco(Object valor) {
        if (valor instanceof Persistente) {
            return ((Persistente) valor).getId();
        }
        if (valor instanceof Enum) {
            return ((Enum<?>) valor).name();
        }
        if (valor instanceof LocalDateTime) {
            return Timestamp.valueOf((LocalDateTime) valor);
        }
        return valor;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object lerValor(ResultSet rs, String coluna, Class<?> tipo) throws SQLException {
        if (rs.getObject(coluna) == null) {
            return null;
        }
        if (tipo == Long.class) {
            return rs.getLong(coluna);
        }
        if (tipo == Integer.class) {
            return rs.getInt(coluna);
        }
        if (tipo == BigDecimal.class) {
            return rs.getBigDecimal(coluna);
        }
        if (tipo == LocalDateTime.class) {
            return rs.getTimestamp(coluna).toLocalDateTime();
        }
        if (tipo.isEnum()) {
            return Enum.valueOf((Class<Enum>) tipo, rs.getString(coluna));
        }
        return rs.getString(coluna);
    }

    private T novaInstancia() {
        try {
            return getTipoClasse().getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Classe " + getTipoClasse().getSimpleName()
                    + " precisa de um construtor sem argumentos", e);
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
