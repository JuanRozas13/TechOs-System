package br.com.bugnacio.techos.repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import br.com.bugnacio.techos.model.OrdemServico;
import br.com.bugnacio.techos.model.StatusOS;

// Interface: o Spring Data JPA escreve a implementação para nós.
//
// @EntityGraph: as telas mostram OS + equipamento + cliente. Com ele, o Spring busca
// os três de uma vez só (um SELECT com JOIN) em vez de fazer um SELECT extra para
// cada linha da tabela (problema conhecido como "N+1").
public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    // Lista de todas as OS (tela /os).
    @Override
    @EntityGraph(attributePaths = { "equipamento", "equipamento.cliente" })
    List<OrdemServico> findAll(Sort sort);

    // Tela de detalhe (/os/{id}).
    @Override
    @EntityGraph(attributePaths = { "equipamento", "equipamento.cliente" })
    Optional<OrdemServico> findById(Long id);

    // Filtro por status (/os?status=EM_ANALISE).
    @EntityGraph(attributePaths = { "equipamento", "equipamento.cliente" })
    List<OrdemServico> findByStatus(StatusOS status, Sort sort);

    // "Últimas ordens de serviço" do painel: as 5 mais recentes.
    @EntityGraph(attributePaths = { "equipamento", "equipamento.cliente" })
    List<OrdemServico> findTop5ByOrderByIdDesc();

    // Quantas OS estão em um grupo de status (abertas ou finalizadas).
    long countByStatusIn(Collection<StatusOS> status);

    // Usado para impedir a exclusão de um equipamento que já tem OS.
    boolean existsByEquipamentoId(Long equipamentoId);

    // Soma dos valores das OS em um grupo de status (faturamento).
    // Devolve null quando não existe nenhuma OS no grupo; quem chama trata isso.
    @Query("select sum(o.valor) from OrdemServico o where o.status in :status")
    BigDecimal somarValorPorStatus(@Param("status") Collection<StatusOS> status);
}