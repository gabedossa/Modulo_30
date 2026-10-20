package br.com.ebac;

import br.com.ebac.dao.IVendaDAO;
import br.com.ebac.domain.Venda;

public class VendaDAOMock extends GenericDAOMock<Venda, String> implements IVendaDAO {

    public VendaDAOMock() {
        super(Venda::getCodigo);
    }
}
