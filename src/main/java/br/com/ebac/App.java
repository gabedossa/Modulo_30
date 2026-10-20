package br.com.ebac;

import br.com.ebac.dao.jdbc.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class App {

    public static void main(String[] args) {
        try (Connection connection = ConnectionFactory.getConnection()) {
            System.out.println("Conectado ao PostgreSQL: " + connection.getMetaData().getDatabaseProductVersion());
        } catch (SQLException e) {
            System.err.println("Erro ao conectar: " + e.getMessage());
        }
    }
}
