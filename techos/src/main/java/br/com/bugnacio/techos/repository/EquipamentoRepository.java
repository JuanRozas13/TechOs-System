package br.com.bugnacio.techos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bugnacio.techos.model.Equipamento;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    // Busca por marca OU modelo, sem diferenciar maiúsculas de minúsculas.
    // O retorno é List<Equipamento> (antes era Object, o que escondia o tipo real).
    List<Equipamento> findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(String marca, String modelo);

    // Usado para impedir a exclusão de um cliente que ainda tem equipamentos.
    // "ClienteId" = o id do campo cliente da entidade Equipamento.
    boolean existsByClienteId(Long clienteId);
}