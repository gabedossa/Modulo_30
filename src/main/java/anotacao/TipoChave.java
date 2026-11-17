package anotacao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca o atributo que é a chave da entidade (usado nas buscas).
 * O valor é o nome do getter que devolve essa chave.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TipoChave {

    String value();
}
