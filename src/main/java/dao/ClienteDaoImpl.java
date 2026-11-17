package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import factory.ConnectionFactory;
import model.Cliente;

public class ClienteDaoImpl implements ClienteDAO {

    private static final String COLUNAS =
            "id, nome, cpf, tel, endereco, numero, cidade, estado, email, qtd_compras, data_nascimento";

    @Override
    public Integer cadastrar(Cliente cli) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlInsert(), Statement.RETURN_GENERATED_KEYS);
            adicionarParametros(stm, cli);
            stm.executeUpdate();

            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return null;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlInsert() {
        return "INSERT INTO tb_cliente (nome, cpf, tel, endereco, numero, cidade, estado, email, qtd_compras, "
                + "data_nascimento) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    }

    /** Parâmetros 1 a 10, comuns ao INSERT e ao UPDATE. */
    private void adicionarParametros(PreparedStatement stm, Cliente cliente) throws Exception {
        stm.setString(1, cliente.getNome());
        stm.setLong(2, cliente.getCpf());
        stm.setObject(3, cliente.getTel(), Types.BIGINT);
        stm.setString(4, cliente.getEnd());
        stm.setObject(5, cliente.getNumero(), Types.INTEGER);
        stm.setString(6, cliente.getCidade());
        stm.setString(7, cliente.getEstado());
        stm.setString(8, cliente.getEmail());
        stm.setInt(9, cliente.getQtdCompras());
        stm.setObject(10, cliente.getDataNascimento(), Types.DATE);
    }

    @Override
    public Integer atualizar(Cliente cliente) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement(getSqlUpdate());
            adicionarParametros(stm, cliente);
            stm.setLong(11, cliente.getId());
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private String getSqlUpdate() {
        return "UPDATE tb_cliente SET nome = ?, cpf = ?, tel = ?, endereco = ?, numero = ?, "
                + "cidade = ?, estado = ?, email = ?, qtd_compras = ?, data_nascimento = ? WHERE id = ?";
    }

    @Override
    public Cliente buscaCliente(Long cpf) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT " + COLUNAS + " FROM tb_cliente WHERE cpf = ?");
            stm.setLong(1, cpf);

            try (ResultSet rs = stm.executeQuery()) {
                if (rs.next()) {
                    return montaCliente(rs);
                }
            }
            return null;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public List<Cliente> buscatodos() throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;
        List<Cliente> clientes = new ArrayList<>();

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("SELECT " + COLUNAS + " FROM tb_cliente");

            try (ResultSet rs = stm.executeQuery()) {
                while (rs.next()) {
                    clientes.add(montaCliente(rs));
                }
            }
            return clientes;
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    @Override
    public Integer excluir(Cliente cli) throws Exception {
        Connection conn = null;
        PreparedStatement stm = null;

        try {
            conn = ConnectionFactory.getConnection();
            stm = conn.prepareStatement("DELETE FROM tb_cliente WHERE id = ?");
            stm.setLong(1, cli.getId());
            return stm.executeUpdate();
        } finally {
            if (stm != null) {
                stm.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    private Cliente montaCliente(ResultSet rs) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getLong("id"));
        cliente.setNome(rs.getString("nome"));
        cliente.setCpf(rs.getLong("cpf"));
        cliente.setTel(rs.getObject("tel", Long.class));
        cliente.setEnd(rs.getString("endereco"));
        cliente.setNumero(rs.getObject("numero", Integer.class));
        cliente.setCidade(rs.getString("cidade"));
        cliente.setEstado(rs.getString("estado"));
        cliente.setEmail(rs.getString("email"));
        cliente.setQtdCompras(rs.getInt("qtd_compras"));
        cliente.setDataNascimento(rs.getObject("data_nascimento", LocalDate.class));
        return cliente;
    }
}
