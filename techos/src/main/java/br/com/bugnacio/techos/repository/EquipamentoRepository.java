package br.com.bugnacio.techos.repository;

import org.jspecify.annotations.Nullable;
import org.springframework.data.jpa.repository.JpaRepository;

import br.com.bugnacio.techos.model.Equipamento;

public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {

    @Nullable
    Object findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(String busca, String busca2);
}