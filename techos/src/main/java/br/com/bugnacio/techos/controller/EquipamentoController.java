package br.com.bugnacio.techos.controller;

import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.bugnacio.techos.model.Cliente;
import br.com.bugnacio.techos.model.Equipamento;
import br.com.bugnacio.techos.model.TipoEquipmento;
import br.com.bugnacio.techos.repository.ClienteRepository;
import br.com.bugnacio.techos.repository.EquipamentoRepository;
import br.com.bugnacio.techos.repository.OrdemServicoRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/equipamentos")
public class EquipamentoController {

    private final EquipamentoRepository repo;
    private final ClienteRepository clienteRepo;
    private final OrdemServicoRepository osRepo;

    public EquipamentoController(
            EquipamentoRepository repo,
            ClienteRepository clienteRepo,
            OrdemServicoRepository osRepo) {
        this.repo = repo;
        this.clienteRepo = clienteRepo;
        this.osRepo = osRepo;
    }

    // Envia para o formulario as listas dos selects (cliente e tipo).
    // Só é chamado onde o formulario aparece, assim a lista de equipamentos não
    // precisa buscar todos os clientes à toa.
    private void prepararFormulario(Model model) {
        model.addAttribute("clientes", clienteRepo.findAll(Sort.by("nome")));
        model.addAttribute("tipos", TipoEquipmento.values());
    }

    // READ: lista (com busca opcional por marca ou modelo).
    @GetMapping
    public String listar(
            @RequestParam(value = "busca", required = false) String busca,
            Model model) {
        if (busca != null && !busca.isBlank()) {
            String termo = busca.trim();
            model.addAttribute("equipamentos",
                    repo.findByMarcaContainingIgnoreCaseOrModeloContainingIgnoreCase(termo, termo));
        } else {
            model.addAttribute("equipamentos", repo.findAll(Sort.by("marca")));
        }
        model.addAttribute("busca", busca);
        return "equipamentos/lista";
    }

    // CREATE (1/2): formulario vazio.
    @GetMapping("/novo")
    public String novo(Model model) {
        Equipamento equipamento = new Equipamento();
        equipamento.setCliente(new Cliente()); // evita erro de null no formulario
        model.addAttribute("equipamento", equipamento);
        prepararFormulario(model);
        return "equipamentos/form";
    }

    // CREATE (2/2) e UPDATE
    @PostMapping("/salvar")
    public String salvar(
            @Valid Equipamento equipamento,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {

        boolean novoCadastro = equipamento.getId() == null;

        // O select envia "cliente.id" vazio quando nenhum cliente foi escolhido.
        // Aqui buscamos o cliente REAL no banco: assim garantimos que ele existe
        // e ignoramos qualquer outro dado de cliente que viesse junto no formulario.
        Long clienteId = (equipamento.getCliente() != null) ? equipamento.getCliente().getId() : null;
        Cliente cliente = (clienteId != null) ? clienteRepo.findById(clienteId).orElse(null) : null;

        if (cliente == null) {
            // Equipamento sem dono quebraria as telas de OS, por isso o cliente é obrigatorio.
            result.rejectValue("cliente", "obrigatorio", "Escolha o cliente dono do equipamento.");
            equipamento.setCliente(new Cliente()); // mantem o formulario funcionando
        } else {
            equipamento.setCliente(cliente);
        }

        if (result.hasErrors()) {
            prepararFormulario(model);
            return "equipamentos/form";
        }

        repo.save(equipamento);

        redirect.addFlashAttribute("mensagem",
                novoCadastro ? "Equipamento cadastrado com sucesso!" : "Equipamento atualizado com sucesso!");
        return "redirect:/equipamentos";
    }

    // UPDATE (1/2): formulario preenchido.
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id,
            Model model, RedirectAttributes redirect) {
        Optional<Equipamento> equipamento = repo.findById(id);
        if (equipamento.isEmpty()) {
            redirect.addFlashAttribute("erro", "Equipamento não encontrado.");
            return "redirect:/equipamentos";
        }
        Equipamento e = equipamento.get();
        if (e.getCliente() == null) {
            e.setCliente(new Cliente());
        }
        model.addAttribute("equipamento", e);
        prepararFormulario(model);
        return "equipamentos/form";
    }

    // DELETE.
    // Equipamento que já tem ordem de serviço não pode ser excluído (o histórico seria perdido).
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirect) {
        if (osRepo.existsByEquipamentoId(id)) {
            redirect.addFlashAttribute("erro",
                    "Não é possível excluir: este equipamento tem ordens de serviço.");
            return "redirect:/equipamentos";
        }

        repo.deleteById(id);
        redirect.addFlashAttribute("mensagem", "Equipamento excluído.");
        return "redirect:/equipamentos";
    }
}