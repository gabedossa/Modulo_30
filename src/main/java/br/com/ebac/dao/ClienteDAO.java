package br.com.ebac.dao;

import br.com.ebac.dao.jdbc.ConnectionFactory;
import br.com.ebac.domain.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO implements IClienteDAO {

    @Override
    public Integer cadastrar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO tb_cliente (nome, cpf, cidade, endereco, estado, casa_numero, tel) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            preencherParametros(stm, cliente);
            int linhas = stm.executeUpdate();
            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    cliente.setId(rs.getLong(1));
                }
            }
            return linhas;
        }
    }

    @Override
    public Cliente buscarPorCpf(String cpf) throws SQLException {
        String sql = "SELECT * FROM tb_cliente WHERE cpf = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setString(1, cpf);
            try (ResultSet rs = stm.executeQuery()) {
                return rs.next() ? converter(rs) : null;
            }
        }
    }

    @Override
    public List<Cliente> buscarTodos() throws SQLException {
        String sql = "SELECT * FROM tb_cliente ORDER BY id";
        List<Cliente> clientes = new ArrayList<>();
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                clientes.add(converter(rs));
            }
        }
        return clientes;
    }

    @Override
    public Integer atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE tb_cliente SET nome = ?, cpf = ?, cidade = ?, endereco = ?, estado = ?, "
                + "casa_numero = ?, tel = ? WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            preencherParametros(stm, cliente);
            stm.setLong(8, cliente.getId());
            return stm.executeUpdate();
        }
    }

    @Override
    public Integer excluir(Cliente cliente) throws SQLException {
        String sql = "DELETE FROM tb_cliente WHERE id = ?";
        try (Connection connection = ConnectionFactory.getConnection();
             PreparedStatement stm = connection.prepareStatement(sql)) {
            stm.setLong(1, cliente.getId());
            return stm.executeUpdate();
        }
    }

    private void preencherParametros(PreparedStatement stm, Cliente cliente) throws SQLException {
        stm.setString(1, cliente.getNome());
        stm.setString(2, cliente.getCpf());
        stm.setString(3, cliente.getCidade());
        stm.setString(4, cliente.getEndereco());
        stm.setString(5, cliente.getEstado());
        stm.setObject(6, cliente.getCasaNumero());
        stm.setString(7, cliente.getTel());
    }

    static Cliente converter(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getString("cpf"));
        cliente.setCidade(rs.getString("cidade"));
        cliente.setEndereco(rs.getString("endereco"));
        cliente.setEstado(rs.getString("estado"));
        cliente.setCasaNumero((Integer) rs.getObject("casa_numero"));
        cliente.setTel(rs.getString("tel"));
        return cliente;
    }
}
