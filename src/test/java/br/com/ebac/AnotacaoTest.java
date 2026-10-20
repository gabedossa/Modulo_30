package br.com.ebac;

import br.com.ebac.anotacao.AnotacaoUtil;
import br.com.ebac.anotacao.ColunaTabela;
import br.com.ebac.domain.Cliente;
import br.com.ebac.domain.Produto;
import br.com.ebac.domain.ProdutoQuantidade;
import br.com.ebac.domain.Venda;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class AnotacaoTest {

    @Test
    public void deveMapearAsTabelas() {
        assertEquals("tb_cliente", AnotacaoUtil.getTabela(Cliente.class));
        assertEquals("tb_produto", AnotacaoUtil.getTabela(Produto.class));
        assertEquals("tb_venda", AnotacaoUtil.getTabela(Venda.class));
        assertEquals("tb_produto_quantidade", AnotacaoUtil.getTabela(ProdutoQuantidade.class));
    }

    @Test
    public void deveMapearAsChaves() {
        assertEquals("cpf", AnotacaoUtil.getColuna(AnotacaoUtil.getCampoChave(Cliente.class)));
        assertEquals("codigo", AnotacaoUtil.getColuna(AnotacaoUtil.getCampoChave(Produto.class)));
        assertEquals("codigo", AnotacaoUtil.getColuna(AnotacaoUtil.getCampoChave(Venda.class)));
    }

    @Test
    public void deveLerValorDaChave() {
        Cliente cliente = new Cliente();
        cliente.setCpf("11111111111");

        assertEquals("11111111111", AnotacaoUtil.getValorChave(cliente));
    }

    @Test
    public void deveMapearTodasAsColunasDoCliente() {
        assertEquals(8, AnotacaoUtil.getCampos(Cliente.class).size());
        assertEquals("casa_numero", AnotacaoUtil.getColuna(AnotacaoUtil.getCampos(Cliente.class).get(6)));
    }

    @Test
    public void listaDeProdutosDaVendaNaoEColuna() {
        for (Field campo : AnotacaoUtil.getCampos(Venda.class)) {
            assertFalse(campo.getName().equals("produtos"));
        }
    }

    @Test
    public void todoSetJavaNameDeveExistir() {
        for (Class<?> classe : new Class<?>[]{Cliente.class, Produto.class, Venda.class, ProdutoQuantidade.class}) {
            for (Field campo : AnotacaoUtil.getCampos(classe)) {
                String setter = campo.getAnnotation(ColunaTabela.class).setJavaName();
                try {
                    classe.getMethod(setter, campo.getType());
                } catch (NoSuchMethodException e) {
                    fail(classe.getSimpleName() + " não tem o método " + setter);
                }
            }
        }
    }

    @Test(expected = IllegalStateException.class)
    public void classeSemTabelaDeveFalhar() {
        AnotacaoUtil.getTabela(String.class);
    }
}
