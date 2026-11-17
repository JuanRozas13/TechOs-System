package br.com.bugnacio.techos.controller;

import java.math.BigDecimal;

// Importa a anotação que transforma a classe em um Controller do Spring
import org.springframework.stereotype.Controller;

// Importa o Model, usado para enviar dados para o HTML
import org.springframework.ui.Model;

// Importa a anotação para criar rotas GET
import org.springframework.web.bind.annotation.GetMapping;

import br.com.bugnacio.techos.model.StatusOS;
import br.com.bugnacio.techos.repository.ClienteRepository;
import br.com.bugnacio.techos.repository.EquipamentoRepository;
import br.com.bugnacio.techos.repository.OrdemServicoRepository;

// Indica que esta classe é um Controller
@Controller
public class HomeController {

    // Repositórios: dão acesso às tabelas do banco de dados
    private final ClienteRepository clienteRepository;
    private final EquipamentoRepository equipamentoRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    // Construtor da classe
    // O Spring injeta automaticamente os repositórios aqui
    public HomeController(
            ClienteRepository clienteRepository,
            EquipamentoRepository equipamentoRepository,
            OrdemServicoRepository ordemServicoRepository) {
        this.clienteRepository = clienteRepository;
        this.equipamentoRepository = equipamentoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
    }

    // Quando alguém acessar a rota "/"
    @GetMapping("/")
    public String inicio(Model model) {

        // Totais de clientes e equipamentos cadastrados
        model.addAttribute("totalClientes", clienteRepository.count());
        model.addAttribute("totalEquipamentos", equipamentoRepository.count());

        // OS abertas = ainda na oficina (Recebido, Em análise, Em manutenção)
        model.addAttribute("osAbertas", ordemServicoRepository.countByStatusIn(StatusOS.ABERTOS));

        // OS concluídas = serviço feito (Concluído e Entregue)
        model.addAttribute("osConcluidas", ordemServicoRepository.countByStatusIn(StatusOS.FINALIZADOS));

        // Faturamento = soma dos valores das OS concluídas ou entregues.
        // Se ainda não existe nenhuma, a soma vem null e usamos zero.
        BigDecimal faturamento = ordemServicoRepository.somarValorPorStatus(StatusOS.FINALIZADOS);
        model.addAttribute("faturamento", faturamento != null ? faturamento : BigDecimal.ZERO);

        // As 5 ordens de serviço mais recentes
        model.addAttribute("ultimasOs", ordemServicoRepository.findTop5ByOrderByIdDesc());

        // Retorna a página dashboard.html
        return "dashboard";
    }
}