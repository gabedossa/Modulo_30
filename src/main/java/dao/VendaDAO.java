package dao;

import java.util.List;

import model.Venda;

public interface VendaDAO {
    Integer cadastrar(Venda venda) throws Exception;
    Integer atualizar(Venda venda) throws Exception;
    Venda buscaVenda(String codigo) throws Exception;
    List<Venda> buscatodos() throws Exception;
    Integer excluir(Venda venda) throws Exception;
}
