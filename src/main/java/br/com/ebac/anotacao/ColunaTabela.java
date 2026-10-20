package br.com.ebac.anotacao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Liga um atributo da entidade a uma coluna da tabela.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface ColunaTabela {

    /** Nome da coluna no banco. */
    String dbName();

    /** Setter usado para preencher o atributo com o valor lido do banco. */
    String setJavaName();
}
