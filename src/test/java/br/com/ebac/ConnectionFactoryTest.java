package br.com.ebac;

import br.com.ebac.dao.jdbc.ConnectionFactory;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ConnectionFactoryTest {

    @Test
    public void deveConectarNoBanco() throws SQLException {
        try (Connection connection = ConnectionFactory.getConnection()) {
            assertNotNull(connection);
            assertTrue(connection.isValid(2));
        }
    }
}
