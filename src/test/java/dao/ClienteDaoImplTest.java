package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import model.Cliente;

class ClienteDaoImplTest {

    private final ClienteDaoImpl dao = new ClienteDaoImpl();
    private final List<Cliente> criados = new ArrayList<>();

    /** CPF aleatório de 11 dígitos, para não colidir com dados já existentes. */
    private Long cpfAleatorio() {
        return ThreadLocalRandom.current().nextLong(10_000_000_000L, 99_999_999_999L);
    }

    private Cliente novoCliente(String nome) throws Exception {
        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpfAleatorio());
        cliente.setTel(11999998888L);
        cliente.setEnd("Rua de Teste");
        cliente.setNumero(10);
        cliente.setCidade("Sao Paulo");
        cliente.setEstado("SP");
        cliente.setEmail("teste@email.com");
        cliente.setQtdCompras(2);
        Integer id = dao.cadastrar(cliente);
        cliente.setId(id.longValue());
        criados.add(cliente);
        return cliente;
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Cliente cliente : criados) {
            try {
                dao.excluir(cliente);
            } catch (Exception e) {

            }
        }
        criados.clear();
    }

    @Test
    void cadastrarDeveGerarIdEPersistirCliente() throws Exception {
        Cliente cliente = novoCliente("Joao da Silva");

        assertNotNull(cliente.getId());

        Cliente encontrado = dao.buscaCliente(cliente.getCpf());
        assertNotNull(encontrado);
        assertEquals(cliente.getCpf(), encontrado.getCpf());
        assertEquals("Joao da Silva", encontrado.getNome());
        assertEquals(11999998888L, encontrado.getTel());
        assertEquals("Rua de Teste", encontrado.getEnd());
        assertEquals(10, encontrado.getNumero());
        assertEquals("Sao Paulo", encontrado.getCidade());
        assertEquals("SP", encontrado.getEstado());
        assertEquals("teste@email.com", encontrado.getEmail());
        assertEquals(2, encontrado.getQtdCompras());
    }

    @Test
    void atualizarDeveAlterarOsDadosDoCliente() throws Exception {
        Cliente cliente = novoCliente("Maria Souza");

        cliente.setNome("Maria Souza Atualizada");
        cliente.setCpf(cpfAleatorio());
        cliente.setCidade("Rio de Janeiro");
        cliente.setEstado("RJ");
        cliente.setEmail("maria.atualizada@email.com");
        cliente.setQtdCompras(3);
        Integer linhasAfetadas = dao.atualizar(cliente);

        assertEquals(1, linhasAfetadas);

        Cliente encontrado = dao.buscaCliente(cliente.getCpf());
        assertNotNull(encontrado);
        assertEquals("Maria Souza Atualizada", encontrado.getNome());
        assertEquals("Rio de Janeiro", encontrado.getCidade());
        assertEquals("RJ", encontrado.getEstado());
        assertEquals("maria.atualizada@email.com", encontrado.getEmail());
        assertEquals(3, encontrado.getQtdCompras());
    }

    @Test
    void buscaClienteDeveRetornarNuloQuandoCpfNaoExiste() throws Exception {
        Cliente encontrado = dao.buscaCliente(-1L);
        assertNull(encontrado);
    }

    @Test
    void buscatodosDeveIncluirClientesCadastrados() throws Exception {
        Cliente cliente1 = novoCliente("Cliente Um");
        Cliente cliente2 = novoCliente("Cliente Dois");

        List<Cliente> todos = dao.buscatodos();

        assertTrue(todos.stream().anyMatch(c -> c.getCpf().equals(cliente1.getCpf())));
        assertTrue(todos.stream().anyMatch(c -> c.getCpf().equals(cliente2.getCpf())));
    }

    @Test
    void excluirDeveRemoverClienteDoBanco() throws Exception {
        Cliente cliente = novoCliente("Cliente Para Excluir");

        Integer linhasAfetadas = dao.excluir(cliente);
        assertEquals(1, linhasAfetadas);

        Cliente encontrado = dao.buscaCliente(cliente.getCpf());
        assertNull(encontrado);

        criados.remove(cliente);
    }

    public ClienteDaoImpl getDao() {
        return dao;
    }
}
