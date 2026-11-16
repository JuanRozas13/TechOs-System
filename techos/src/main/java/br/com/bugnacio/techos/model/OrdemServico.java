package br.com.bugnacio.techos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class OrdemServico {
    // @id: o campo que representa a chave primária da tabela.
    // @GeneratedValue: o valor da chave primária será gerado automaticamente pelo banco de dados.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // criar coluna entrada
    private String entrada;

    // @manytoone: uma os pertence a um cliente. @joincolumn: cria a coluna cliente_id (chave estrangeira) na tabela.
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;
 
    // criar coluna equipamento que vai receber o nome do equipamento cadastrado no equipamento
    private String equipamento;

    // criar coluna defeito
    private String defeito;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntrada() {
        return entrada;
    }

    public void setEntrada(String entrada) {
        this.entrada = entrada;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(String equipamento) {
        this.equipamento = equipamento;
    }

    public String getDefeito() {
        return defeito;
    }

    public void setDefeito(String defeito) {
        this.defeito = defeito;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getValor() {
        return valor;
    }

    public void setValor(String valor) {
        this.valor = valor;
    }

    // criar coluna status
    private String status;

    // criar coluna valor
    private String valor;
}
