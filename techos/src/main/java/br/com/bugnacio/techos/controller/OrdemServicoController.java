package br.com.bugnacio.techos.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.bugnacio.techos.model.Equipamento;
import br.com.bugnacio.techos.model.OrdemServico;
import br.com.bugnacio.techos.model.StatusOS;
import br.com.bugnacio.techos.repository.EquipamentoRepository;
import br.com.bugnacio.techos.repository.OrdemServicoRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/os")
public class OrdemServicoController {

    // Limites iguais aos das colunas do banco (veja a entidade OrdemServico).
    private static final int DIAGNOSTICO_MAXIMO = 1000;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999.99");

    private final OrdemServicoRepository repo;
    private final EquipamentoRepository equipamentoRepo;

    public OrdemServicoController(OrdemServicoRepository repo, EquipamentoRepository equipamentoRepo) {
        this.repo = repo;
        this.equipamentoRepo = equipamentoRepo;
    }

    // SEGURANÇA: o formulário de "Nova OS" só tem dois campos (equipamento e defeito).
    // Sem esta trava, alguém poderia enviar também "status", "valor" ou "id" pelo navegador
    // e o Spring aceitaria. Com ela, qualquer outro campo é simplesmente ignorado.
    @InitBinder("ordemServico")
    public void limitarCamposDoFormulario(WebDataBinder binder) {
        binder.setAllowedFields("equipamento.id", "defeito");
    }

    // Envia para o formulário a lista do select de equipamentos.
    private void carregarEquipamentos(Model model) {
        model.addAttribute("equipamentos", equipamentoRepo.findAll(Sort.by("marca")));
    }

    // Resposta padrão quando o id da URL não existe.
    private String naoEncontrada(RedirectAttributes redirect) {
        redirect.addFlashAttribute("erro", "Ordem de serviço não encontrada.");
        return "redirect:/os";
    }

    // READ: lista, com filtro opcional por status.
    // Endereço: http://localhost:8080/os  ou  /os?status=EM_ANALISE
    @GetMapping
    public String listar(
            @RequestParam(value = "status", required = false) StatusOS status,
            Model model) {

        // As mais recentes aparecem primeiro.
        Sort maisRecentesPrimeiro = Sort.by(Sort.Direction.DESC, "id");

        List<OrdemServico> ordens = (status == null)
                ? repo.findAll(maisRecentesPrimeiro)
                : repo.findByStatus(status, maisRecentesPrimeiro);

        model.addAttribute("ordens", ordens);
        model.addAttribute("statusList", StatusOS.values());
        model.addAttribute("statusFiltro", status);
        return "os/lista";
    }

    // CREATE (1/2): formulário vazio.
    @GetMapping("/nova")
    public String nova(Model model) {
        model.addAttribute("ordemServico", new OrdemServico());
        carregarEquipamentos(model);
        return "os/form";
    }

    // CREATE (2/2): valida e salva.
    // A data de entrada e o status inicial (Recebido) são preenchidos pela própria entidade.
    @PostMapping("/salvar")
    public String salvar(
            @Valid OrdemServico ordemServico,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        // O select envia "equipamento.id". Buscamos o equipamento REAL no banco
        // para garantir que ele existe.
        Long equipamentoId = (ordemServico.getEquipamento() != null)
                ? ordemServico.getEquipamento().getId()
                : null;

        Equipamento equipamento = (equipamentoId != null)
                ? equipamentoRepo.findById(equipamentoId).orElse(null)
                : null;

        // Nenhum equipamento escolhido (ou id inexistente): erro embaixo do select.
        if (equipamento == null && !result.hasFieldErrors("equipamento")) {
            result.rejectValue("equipamento", "obrigatorio", "Escolha o equipamento.");
        }

        // Voltou com erro: reabre o formulário (sem redirecionar) para o usuário corrigir.
        if (result.hasErrors()) {
            carregarEquipamentos(model);
            return "os/form";
        }

        ordemServico.setEquipamento(equipamento);
        ordemServico.setDefeito(ordemServico.getDefeito().trim());
        repo.save(ordemServico);

        redirect.addFlashAttribute("mensagem",
                "Ordem de serviço #" + ordemServico.getId() + " aberta com sucesso!");
        return "redirect:/os/" + ordemServico.getId();
    }

    // READ: detalhe de uma OS.
    @GetMapping("/{id}")
    public String detalhe(@PathVariable("id") Long id, Model model, RedirectAttributes redirect) {
        Optional<OrdemServico> os = repo.findById(id);
        if (os.isEmpty()) {
            return naoEncontrada(redirect);
        }

        model.addAttribute("os", os.get());
        model.addAttribute("statusList", StatusOS.values());
        return "os/detalhe";
    }

    // UPDATE: muda o status (formulário "Mudar status" da tela de detalhe).
    @PostMapping("/{id}/status")
    public String atualizarStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") StatusOS status,
            RedirectAttributes redirect) {

        Optional<OrdemServico> encontrada = repo.findById(id);
        if (encontrada.isEmpty()) {
            return naoEncontrada(redirect);
        }

        OrdemServico os = encontrada.get();
        os.setStatus(status);
        repo.save(os);

        redirect.addFlashAttribute("mensagem", "Status atualizado para " + status.getDescricao() + ".");
        return "redirect:/os/" + id;
    }

    // UPDATE: salva diagnóstico e valor (formulário "Diagnóstico e valor").
    @PostMapping("/{id}/diagnostico")
    public String salvarDiagnostico(
            @PathVariable("id") Long id,
            @RequestParam(value = "diagnostico", required = false) String diagnostico,
            @RequestParam(value = "valor", required = false) BigDecimal valor,
            RedirectAttributes redirect) {

        Optional<OrdemServico> encontrada = repo.findById(id);
        if (encontrada.isEmpty()) {
            return naoEncontrada(redirect);
        }

        // Campo em branco vira null (assim o banco não guarda texto vazio).
        String texto = (diagnostico == null || diagnostico.isBlank()) ? null : diagnostico.trim();

        // Dinheiro sempre com 2 casas decimais.
        BigDecimal valorFinal = (valor == null) ? null : valor.setScale(2, RoundingMode.HALF_UP);

        if (texto != null && texto.length() > DIAGNOSTICO_MAXIMO) {
            redirect.addFlashAttribute("erro",
                    "O diagnóstico pode ter no máximo " + DIAGNOSTICO_MAXIMO + " caracteres.");
            return "redirect:/os/" + id;
        }

        if (valorFinal != null && (valorFinal.signum() < 0 || valorFinal.compareTo(VALOR_MAXIMO) > 0)) {
            redirect.addFlashAttribute("erro", "Informe um valor entre 0,00 e 99.999.999,99.");
            return "redirect:/os/" + id;
        }

        OrdemServico os = encontrada.get();
        os.setDiagnostico(texto);
        os.setValor(valorFinal);
        repo.save(os);

        redirect.addFlashAttribute("mensagem", "Diagnóstico e valor salvos.");
        return "redirect:/os/" + id;
    }
}