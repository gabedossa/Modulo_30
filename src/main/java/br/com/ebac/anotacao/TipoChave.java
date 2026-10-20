package br.com.ebac.anotacao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca o atributo que é a chave de negócio da entidade (ex.: CPF do cliente),
 * usado nas consultas e na verificação de duplicidade.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface TipoChave {
}
