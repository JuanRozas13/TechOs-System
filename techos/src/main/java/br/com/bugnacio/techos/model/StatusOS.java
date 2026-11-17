package br.com.bugnacio.techos.model;

import java.util.List;

// enum: uma lista de valores fixos.
// Aqui ele representa as etapas pelas quais uma ordem de serviço passa.
//
// ATENÇÃO: a ORDEM em que os valores são declarados importa.
// A tela de detalhe usa ordinal() para pintar o "trilho" (Recebido -> ... -> Entregue).
public enum StatusOS {

    RECEBIDO("Recebido"),
    EM_ANALISE("Em análise"),
    EM_MANUTENCAO("Em manutenção"),
    CONCLUIDO("Concluído"),
    ENTREGUE("Entregue");

    // Status em que o equipamento ainda está na oficina (OS aberta).
    public static final List<StatusOS> ABERTOS = List.of(RECEBIDO, EM_ANALISE, EM_MANUTENCAO);

    // Status em que o serviço já foi feito. Só esses entram no faturamento.
    public static final List<StatusOS> FINALIZADOS = List.of(CONCLUIDO, ENTREGUE);

    private final String descricao;

    StatusOS(String descricao) {
        this.descricao = descricao;
    }

    // Texto exibido nas telas: th:text="${os.status.descricao}"
    public String getDescricao() {
        return descricao;
    }
}