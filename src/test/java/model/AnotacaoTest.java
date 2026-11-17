package model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import anotacao.ColunaTabela;
import anotacao.Tabela;
import anotacao.TipoChave;

class AnotacaoTest {

    /** Entidades que têm @TipoChave. */
    private static final Class<?>[] ENTIDADES_COM_CHAVE = { Cliente.class, Produto.class, Venda.class };

    private static final Class<?>[] TODAS = { Cliente.class, Produto.class, Venda.class, ProdutoQuantidade.class };

    private List<String> colunas(Class<?> classe) {
        List<String> nomes = new ArrayList<>();
        for (Field campo : classe.getDeclaredFields()) {
            ColunaTabela coluna = campo.getAnnotation(ColunaTabela.class);
            if (coluna != null) {
                nomes.add(coluna.dbName());
            }
        }
        return nomes;
    }

    private TipoChave chave(Class<?> classe) {
        for (Field campo : classe.getDeclaredFields()) {
            TipoChave chave = campo.getAnnotation(TipoChave.class);
            if (chave != null) {
                return chave;
            }
        }
        return null;
    }

    @Test
    void deveMapearAsTabelasDoBanco() {
        assertEquals("TB_CLIENTE", Cliente.class.getAnnotation(Tabela.class).value());
        assertEquals("TB_PRODUTO", Produto.class.getAnnotation(Tabela.class).value());
        assertEquals("TB_VENDA", Venda.class.getAnnotation(Tabela.class).value());
        assertEquals("TB_PRODUTO_QUANTIDADE", ProdutoQuantidade.class.getAnnotation(Tabela.class).value());
    }

    @Test
    void deveMapearTodasAsColunasDasTabelas() {
        assertEquals(List.of("id", "nome", "cpf", "tel", "endereco", "numero", "cidade", "estado", "email",
                "qtd_compras", "data_nascimento"), colunas(Cliente.class));
        assertEquals(List.of("id", "codigo", "nome", "descricao", "valor", "valor_compra", "disponivel", "marca"),
                colunas(Produto.class));
        assertEquals(List.of("id", "codigo", "id_cliente_fk", "valor_total", "data_venda", "status_venda"),
                colunas(Venda.class));
        assertEquals(List.of("id", "quantidade", "valor_total"), colunas(ProdutoQuantidade.class));
    }

    @Test
    void cadaEntidadeDeveTerSuaChave() {
        assertEquals("getCpf", chave(Cliente.class).value());
        assertEquals("getCodigo", chave(Produto.class).value());
        assertEquals("getCodigo", chave(Venda.class).value());
        assertNull(chave(ProdutoQuantidade.class));
    }

    @Test
    void getterDaChaveDeveExistir() {
        for (Class<?> classe : ENTIDADES_COM_CHAVE) {
            TipoChave chave = chave(classe);
            assertNotNull(chave, classe.getSimpleName() + " sem @TipoChave");
            assertDoesNotThrow(() -> classe.getMethod(chave.value()),
                    classe.getSimpleName() + " não tem o método " + chave.value());
        }
    }

    /**
     * Todo setJavaName precisa existir na classe. A exceção é a coluna id_cliente_fk da Venda:
     * o atributo é um Cliente e o VendaDaoImpl grava/lê o id do cliente diretamente.
     */
    @Test
    void todoSetJavaNameDeveExistir() {
        for (Class<?> classe : TODAS) {
            for (Field campo : classe.getDeclaredFields()) {
                ColunaTabela coluna = campo.getAnnotation(ColunaTabela.class);
                if (coluna == null || coluna.dbName().equals("id_cliente_fk")) {
                    continue;
                }
                assertDoesNotThrow(() -> classe.getMethod(coluna.setJavaName(), campo.getType()),
                        classe.getSimpleName() + " não tem o método " + coluna.setJavaName());
            }
        }
    }

    @Test
    void listaDeProdutosDaVendaNaoEColuna() throws Exception {
        assertNull(Venda.class.getDeclaredField("produtos").getAnnotation(ColunaTabela.class));
        assertNull(ProdutoQuantidade.class.getDeclaredField("produto").getAnnotation(ColunaTabela.class));
    }
}
