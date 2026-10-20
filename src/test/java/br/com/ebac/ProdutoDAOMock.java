package br.com.ebac;

import br.com.ebac.dao.IProdutoDAO;
import br.com.ebac.domain.Produto;

public class ProdutoDAOMock extends GenericDAOMock<Produto, String> implements IProdutoDAO {

    public ProdutoDAOMock() {
        super(Produto::getCodigo);
    }
}
