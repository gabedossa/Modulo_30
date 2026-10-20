package br.com.ebac.domain;

/**
 * Toda entidade gravada no banco tem um id gerado pelo PostgreSQL.
 */
public interface Persistente {

    Long getId();

    void setId(Long id);
}
