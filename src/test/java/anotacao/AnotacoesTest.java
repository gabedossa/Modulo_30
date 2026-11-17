package anotacao;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Test;

/**
 * As annotations precisam ficar disponíveis em tempo de execução (RUNTIME),
 * senão a reflection não consegue lê-las.
 */
class AnotacoesTest {

    @Test
    void tabelaDeveSerLidaEmRuntimeEUsadaEmClasses() {
        assertEquals(RetentionPolicy.RUNTIME, Tabela.class.getAnnotation(Retention.class).value());
        assertArrayEquals(new ElementType[] { ElementType.TYPE }, Tabela.class.getAnnotation(Target.class).value());
    }

    @Test
    void colunaTabelaDeveSerLidaEmRuntimeEUsadaEmAtributos() {
        assertEquals(RetentionPolicy.RUNTIME, ColunaTabela.class.getAnnotation(Retention.class).value());
        assertArrayEquals(new ElementType[] { ElementType.FIELD },
                ColunaTabela.class.getAnnotation(Target.class).value());
    }

    @Test
    void tipoChaveDeveSerLidaEmRuntimeEUsadaEmAtributos() {
        assertEquals(RetentionPolicy.RUNTIME, TipoChave.class.getAnnotation(Retention.class).value());
        assertArrayEquals(new ElementType[] { ElementType.FIELD },
                TipoChave.class.getAnnotation(Target.class).value());
    }
}
