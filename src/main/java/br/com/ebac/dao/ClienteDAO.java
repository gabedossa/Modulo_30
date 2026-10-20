package br.com.ebac.dao;

import br.com.ebac.dao.generic.GenericDAO;
import br.com.ebac.domain.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ClienteDAO extends GenericDAO<Cliente, String> implements IClienteDAO {

    @Override
    protected String getTabela() {
        return "tb_cliente";
    }

    @Override
    protected String getColunaChave() {
        return "cpf";
    }

    @Override
    protected String[] getColunas() {
        return new String[]{"nome", "cpf", "cidade", "endereco", "estado", "casa_numero", "tel"};
    }

    @Override
    protected void preencherParametros(PreparedStatement stm, Cliente cliente) throws SQLException {
        stm.setString(1, cliente.getNome());
        stm.setString(2, cliente.getCpf());
        stm.setString(3, cliente.getCidade());
        stm.setString(4, cliente.getEndereco());
        stm.setString(5, cliente.getEstado());
        stm.setObject(6, cliente.getCasaNumero());
        stm.setString(7, cliente.getTel());
    }

    @Override
    protected Cliente converter(Connection connection, ResultSet rs) throws SQLException {
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
