package factory;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Testa a ConnectionFactory sem banco: o DriverManager é mockado.
 */
class ConnectionFactoryTest {

    @Test
    void getConnectionDeveDevolverAConexaoDoDriverManager() {
        Connection conn = mock(Connection.class);

        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenReturn(conn);

            assertSame(conn, ConnectionFactory.getConnection());
        }
    }

    @Test
    void getConnectionDeveEmbrulharSQLExceptionEmRuntimeException() {
        SQLException falha = new SQLException("Connection refused");

        try (MockedStatic<DriverManager> driver = mockStatic(DriverManager.class)) {
            driver.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString())).thenThrow(falha);

            RuntimeException erro = assertThrows(RuntimeException.class, ConnectionFactory::getConnection);
            assertInstanceOf(SQLException.class, erro.getCause());
            assertSame(falha, erro.getCause());
        }
    }

    @Test
    void construtorDaFabricaDeveSerPrivado() throws Exception {
        Constructor<ConnectionFactory> construtor = ConnectionFactory.class.getDeclaredConstructor();

        assertTrue(Modifier.isPrivate(construtor.getModifiers()));
    }
}
