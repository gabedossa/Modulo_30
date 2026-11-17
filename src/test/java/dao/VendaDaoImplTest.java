package dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import model.Cliente;
import model.Produto;
import model.Venda;

/**
 * Teste de integração: precisa do PostgreSQL com as tabelas do README criadas.
 */
class VendaDaoImplTest {

    private final VendaDaoImpl vendaDao = new VendaDaoImpl();
    private final ClienteDaoImpl clienteDao = new ClienteDaoImpl();
    private final ProdutoDaoImpl produtoDao = new ProdutoDaoImpl();

    private final List<Venda> vendasCriadas = new ArrayList<>();
    private final List<Produto> produtosCriados = new ArrayList<>();
    private Cliente cliente;

    @BeforeEach
    void criarCliente() throws Exception {
        cliente = new Cliente();
        cliente.setNome("Cliente da Venda");
        cliente.setCpf(ThreadLocalRandom.current().nextLong(10_000_000_000L, 99_999_999_999L));
        cliente.setId(clienteDao.cadastrar(cliente).longValue());
    }

    @AfterEach
    void limparRegistrosCriados() {
        for (Venda venda : vendasCriadas) {
            try {
                vendaDao.excluir(venda);
            } catch (Exception e) {

            }
        }
        for (Produto produto : produtosCriados) {
            try {
                produtoDao.excluir(produto);
            } catch (Exception e) {

            }
        }
        try {
            clienteDao.excluir(cliente);
        } catch (Exception e) {

        }
        vendasCriadas.clear();
        produtosCriados.clear();
    }

    private Produto novoProduto(String valor) throws Exception {
        Produto produto = new Produto();
        produto.setCodigo("TESTE-" + UUID.randomUUID());
        produto.setNome("Produto da venda");
        produto.setValor(new BigDecimal(valor));
        produto.setId(produtoDao.cadastrar(produto).longValue());
        produtosCriados.add(produto);
        return produto;
    }

    private Venda novaVenda() {
        Venda venda = new Venda();
        venda.setCodigo("VENDA-" + UUID.randomUUID());
        venda.setCliente(cliente);
        venda.setDataVenda(Instant.now().truncatedTo(ChronoUnit.MILLIS));
        venda.setStatus(Venda.Status.INICIADA);
        vendasCriadas.add(venda);
        return venda;
    }

    @Test
    void cadastrarDeveGravarVendaComSeusProdutos() throws Exception {
        Venda venda = novaVenda();
        venda.adicionarProduto(novoProduto("10.00"), 2);
        venda.adicionarProduto(novoProduto("5.00"), 3);

        vendaDao.cadastrar(venda);
        assertNotNull(venda.getId());

        Venda encontrada = vendaDao.buscaVenda(venda.getCodigo());
        assertNotNull(encontrada);
        assertEquals(Venda.Status.INICIADA, encontrada.getStatus());
        assertEquals(venda.getDataVenda(), encontrada.getDataVenda());
        assertEquals(cliente.getCpf(), encontrada.getCliente().getCpf());
        assertEquals(2, encontrada.getProdutos().size());
        assertEquals(5, encontrada.getQuantidadeTotalProdutos());
        assertEquals(0, new BigDecimal("35.00").compareTo(encontrada.getValorTotal()));
    }

    @Test
    void atualizarDeveConcluirVendaETrocarProdutos() throws Exception {
        Produto produto = novoProduto("10.00");
        Venda venda = novaVenda();
        venda.adicionarProduto(produto, 1);
        vendaDao.cadastrar(venda);

        venda.adicionarProduto(produto, 4);
        venda.setStatus(Venda.Status.CONCLUIDA);
        assertEquals(1, vendaDao.atualizar(venda));

        Venda encontrada = vendaDao.buscaVenda(venda.getCodigo());
        assertEquals(Venda.Status.CONCLUIDA, encontrada.getStatus());
        assertEquals(1, encontrada.getProdutos().size());
        assertEquals(5, encontrada.getQuantidadeTotalProdutos());
        assertEquals(0, new BigDecimal("50.00").compareTo(encontrada.getValorTotal()));
    }

    @Test
    void buscaVendaDeveRetornarNuloQuandoCodigoNaoExiste() throws Exception {
        assertNull(vendaDao.buscaVenda("INEXISTENTE-" + UUID.randomUUID()));
    }

    @Test
    void buscatodosDeveIncluirVendaCadastrada() throws Exception {
        Venda venda = novaVenda();
        venda.adicionarProduto(novoProduto("1.00"), 1);
        vendaDao.cadastrar(venda);

        List<Venda> todas = vendaDao.buscatodos();

        assertTrue(todas.stream().anyMatch(v -> v.getCodigo().equals(venda.getCodigo())
                && v.getProdutos().size() == 1));
    }

    @Test
    void excluirDeveRemoverVendaEItens() throws Exception {
        Venda venda = novaVenda();
        venda.adicionarProduto(novoProduto("1.00"), 1);
        vendaDao.cadastrar(venda);

        assertEquals(1, vendaDao.excluir(venda));
        assertNull(vendaDao.buscaVenda(venda.getCodigo()));
        vendasCriadas.remove(venda);
    }

    @Test
    void naoDeveExcluirProdutoQueEstaEmUmaVenda() throws Exception {
        Produto produto = novoProduto("1.00");
        Venda venda = novaVenda();
        venda.adicionarProduto(produto, 1);
        vendaDao.cadastrar(venda);

        assertThrows(Exception.class, () -> produtoDao.excluir(produto));
    }
}
