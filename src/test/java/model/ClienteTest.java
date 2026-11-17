package model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ClienteTest {

    @Test
    void clienteNovoDeveComecarComZeroCompras() {
        Cliente cliente = new Cliente();

        assertEquals(0, cliente.getQtdCompras());
    }

    @Test
    void camposOpcionaisDevemComecarNulos() {
        Cliente cliente = new Cliente();

        assertNull(cliente.getId());
        assertNull(cliente.getTel());
        assertNull(cliente.getNumero());
        assertNull(cliente.getEmail());
        assertNull(cliente.getDataNascimento());
    }

    @Test
    void gettersDevemDevolverOQueFoiDefinidoNosSetters() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Joao da Silva");
        cliente.setCpf(12345678901L);
        cliente.setTel(11999998888L);
        cliente.setEnd("Rua das Flores");
        cliente.setNumero(100);
        cliente.setCidade("Sao Paulo");
        cliente.setEstado("SP");
        cliente.setEmail("joao@email.com");
        cliente.setQtdCompras(5);
        cliente.setDataNascimento(LocalDate.of(1990, 5, 20));

        assertEquals(1L, cliente.getId());
        assertEquals("Joao da Silva", cliente.getNome());
        assertEquals(12345678901L, cliente.getCpf());
        assertEquals(11999998888L, cliente.getTel());
        assertEquals("Rua das Flores", cliente.getEnd());
        assertEquals(100, cliente.getNumero());
        assertEquals("Sao Paulo", cliente.getCidade());
        assertEquals("SP", cliente.getEstado());
        assertEquals("joao@email.com", cliente.getEmail());
        assertEquals(5, cliente.getQtdCompras());
        assertEquals(LocalDate.of(1990, 5, 20), cliente.getDataNascimento());
    }

    @Test
    void clienteDeveSerPersistente() {
        assertInstanceOf(Persistente.class, new Cliente());
    }
}
