package br.com.ebac;

import br.com.ebac.domain.Cliente;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ClienteTest {

    @Test
    public void deveGuardarOsDadosDoCliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setNome("Gabriel");
        cliente.setCpf("12345678900");
        cliente.setCidade("São Paulo");
        cliente.setEndereco("Rua das Flores");
        cliente.setEstado("SP");
        cliente.setCasaNumero(123);
        cliente.setTel("11999999999");

        assertEquals(Long.valueOf(1L), cliente.getId());
        assertEquals("Gabriel", cliente.getNome());
        assertEquals("12345678900", cliente.getCpf());
        assertEquals("São Paulo", cliente.getCidade());
        assertEquals("Rua das Flores", cliente.getEndereco());
        assertEquals("SP", cliente.getEstado());
        assertEquals(Integer.valueOf(123), cliente.getCasaNumero());
        assertEquals("11999999999", cliente.getTel());
    }

    @Test
    public void novoClienteDeveComecarVazio() {
        Cliente cliente = new Cliente();

        assertNull(cliente.getId());
        assertNull(cliente.getNome());
        assertNull(cliente.getCpf());
        assertNull(cliente.getCasaNumero());
    }
}
