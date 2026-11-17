package br.com.bugnacio.techos.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

// @Entity: esta classe vira a tabela ordem_servico no banco.
@Entity
public class OrdemServico {

    // @Id: chave primária. @GeneratedValue: o banco gera o número sozinho.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // @ManyToOne: muitas ordens de serviço podem ser do MESMO equipamento.
    // O cliente não precisa ser guardado aqui: ele vem do equipamento (os.equipamento.cliente).
    // LAZY: só busca o equipamento no banco quando alguém realmente precisar dele.
    @NotNull(message = "Escolha o equipamento")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipamento_id", nullable = false)
    private Equipamento equipamento;

    // Preenchida automaticamente na criação (veja o método preencherPadroes abaixo).
    @Column(nullable = false)
    private LocalDate dataEntrada;

    @NotBlank(message = "Descreva o defeito relatado")
    @Size(max = 500, message = "O defeito pode ter no máximo 500 caracteres")
    @Column(nullable = false, length = 500)
    private String defeito;

    // Opcional: só é preenchido depois que o técnico analisa o equipamento.
    @Column(length = 1000)
    private String diagnostico;

    // BigDecimal é o tipo correto para dinheiro (double perde centavos).
    // precision = 10 e scale = 2  ->  até 99.999.999,99
    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    // @Enumerated(EnumType.STRING): grava o nome ("EM_ANALISE") e não o número.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOS status;

    // Construtor padrão: o JPA precisa dele.
    public OrdemServico() {
    }

    // @PrePersist: roda automaticamente logo antes de gravar a OS pela primeira vez.
    // É aqui que nascem a data de entrada e o status inicial.
    @PrePersist
    void preencherPadroes() {
        if (dataEntrada == null) {
            dataEntrada = LocalDate.now();
        }
        if (status == null) {
            status = StatusOS.RECEBIDO;
        }

        
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Equipamento getEquipamento() {
        return equipamento;
    }

    public void setEquipamento(Equipamento equipamento) {
        this.equipamento = equipamento;
    }

    public LocalDate getDataEntrada() {
        return dataEntrada;
    }

    public void setDataEntrada(LocalDate dataEntrada) {
        this.dataEntrada = dataEntrada;
    }

    public String getDefeito() {
        return defeito;
    }

    public void setDefeito(String defeito) {
        this.defeito = defeito;
    }

    public String getDiagnostico() {
        return diagnostico;
    }

    public void setDiagnostico(String diagnostico) {
        this.diagnostico = diagnostico;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public StatusOS getStatus() {
        return status;
    }

    public void setStatus(StatusOS status) {
        this.status = status;
    }
}