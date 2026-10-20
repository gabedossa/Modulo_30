package br.com.ebac.domain;

import br.com.ebac.anotacao.ColunaTabela;
import br.com.ebac.anotacao.Tabela;
import br.com.ebac.anotacao.TipoChave;

@Tabela("tb_cliente")
public class Cliente implements Persistente {

    @ColunaTabela(dbName = "id", setJavaName = "setId")
    private Long id;

    @ColunaTabela(dbName = "nome", setJavaName = "setNome")
    private String nome;

    @TipoChave
    @ColunaTabela(dbName = "cpf", setJavaName = "setCpf")
    private String cpf;

    @ColunaTabela(dbName = "cidade", setJavaName = "setCidade")
    private String cidade;

    @ColunaTabela(dbName = "endereco", setJavaName = "setEndereco")
    private String endereco;

    @ColunaTabela(dbName = "estado", setJavaName = "setEstado")
    private String estado;

    @ColunaTabela(dbName = "casa_numero", setJavaName = "setCasaNumero")
    private Integer casaNumero;

    @ColunaTabela(dbName = "tel", setJavaName = "setTel")
    private String tel;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Integer getCasaNumero() {
        return casaNumero;
    }

    public void setCasaNumero(Integer casaNumero) {
        this.casaNumero = casaNumero;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }
}
