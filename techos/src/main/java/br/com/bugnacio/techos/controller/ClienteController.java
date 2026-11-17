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
import br.com.bugnacio.techos.repository.ClienteRepository;
import br.com.bugnacio.techos.repository.EquipamentoRepository;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteRepository repo;
    private final EquipamentoRepository equipamentoRepo;

    public ClienteController(ClienteRepository repo, EquipamentoRepository equipamentoRepo) {
        this.repo = repo;
        this.equipamentoRepo = equipamentoRepo;
    }

    // READ: lista (com busca opcional por nome e ordem alfabetica).
    @GetMapping
    public String listar(
            @RequestParam(value = "busca", required = false) String busca,
            Model model) {
        if (busca != null && !busca.isBlank()) {
            model.addAttribute("clientes", repo.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim()));
        } else {
            model.addAttribute("clientes", repo.findAll(Sort.by("nome")));
        }
        model.addAttribute("busca", busca);
        return "clientes/lista";
    }

    // CREATE (1/2): formulario vazio.
    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "clientes/form";
    }

    // CREATE (2/2) e UPDATE
    // Recebe os dados enviados pelo formulário, valida e salva no banco.
    @PostMapping("/salvar")
    public String salvar(
            @Valid Cliente cliente,
            BindingResult result,
            RedirectAttributes redirect) {

        // Se o ID for null, significa que ainda não existe registro no banco.
        boolean novoCadastro = cliente.getId() == null;

        // Só confere telefone repetido se o telefone passou na validação básica
        // (se estiver vazio, a mensagem "Informe o telefone" já resolve).
        if (!result.hasFieldErrors("telefone")) {

            // Cadastro novo: existe algum cliente com esse telefone?
            // Edição: existe OUTRO cliente com esse telefone? (o próprio é desconsiderado pelo ID)
            boolean jaExiste = novoCadastro
                    ? repo.existsByTelefone(cliente.getTelefone())
                    : repo.existsByTelefoneAndIdNot(cliente.getTelefone(), cliente.getId());

            // Se o telefone já estiver cadastrado, mostra o erro embaixo do campo telefone.
            if (jaExiste) {
                result.rejectValue(
                        "telefone",
                        "duplicado",
                        "Já existe um cliente com esse telefone.");
            }
        }

        // Houve erro (campo vazio, e-mail inválido, telefone duplicado...)?
        // Volta para o formulário sem salvar e sem redirecionar.
        if (result.hasErrors()) {
            return "clientes/form";
        }

        // Dados válidos: o Spring Data JPA decide entre inserir ou atualizar conforme o ID.
        repo.save(cliente);

        redirect.addFlashAttribute(
                "mensagem",
                novoCadastro ? "Cliente cadastrado com sucesso!" : "Cliente atualizado com sucesso!");

        return "redirect:/clientes";
    }

    // UPDATE (1/2): formulario preenchido.
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable("id") Long id,
            Model model, RedirectAttributes redirect) {
        Optional<Cliente> cliente = repo.findById(id);
        if (cliente.isEmpty()) {
            redirect.addFlashAttribute("erro", "Cliente não encontrado.");
            return "redirect:/clientes";
        }
        model.addAttribute("cliente", cliente.get());
        return "clientes/form";
    }

    // DELETE.
    // Cliente com equipamentos não pode ser excluído: o banco recusaria (chave estrangeira)
    // e o usuário veria uma página de erro. Por isso avisamos antes, com uma mensagem clara.
    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable("id") Long id, RedirectAttributes redirect) {
        if (equipamentoRepo.existsByClienteId(id)) {
            redirect.addFlashAttribute("erro",
                    "Não é possível excluir: este cliente tem equipamentos cadastrados.");
            return "redirect:/clientes";
        }

        repo.deleteById(id);
        redirect.addFlashAttribute("mensagem", "Cliente excluído.");
        return "redirect:/clientes";
    }
}