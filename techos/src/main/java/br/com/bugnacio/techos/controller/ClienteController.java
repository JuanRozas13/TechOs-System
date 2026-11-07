package br.com.bugnacio.techos.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.com.bugnacio.techos.model.Cliente;
import br.com.bugnacio.techos.repository.ClienteRepository;

// @Controller: bean do Spring, a recepcao dos pedidos de clientes.
@Controller
// @RequestMapping na classe: o comeco do endereco para TODOS os metodos daqui.
@RequestMapping("/clientes")
public class ClienteController {
    // o controller precisa do repository para acessar o banco de dados.
    private final ClienteRepository repo;

    // Injeção de dependência: o Spring vai criar um ClienteRepository e passar para
    // o controller.
    public ClienteController(ClienteRepository repo) {
        this.repo = repo;
    }

    // @GetMapping vazio = o proprio "/clientes".
    // Endereco completo: http://localhost:8080/clientes
    @GetMapping
    public String listar(Model model) {

        // A tela clientes/lista espera uma lista chamada "clientes".
        // Ainda nao temos banco, entao mandamos uma lista VAZIA (List.of()).
        // A tela mostra o "estado vazio". Na aula 5 essa lista vem do banco.
        model.addAttribute("clientes", repo.findAll());

        // SEM @ResponseBody: o texto devolvido e o nome da tela.
        // Abre: src/main/resources/templates/clientes/lista.html
        return "clientes/lista";

    }

    // GET / CLIENTES/NOVO: MOSTRAR O FORMULARIO PARA CRIAR UM NOVO CLIENTE
    @GetMapping("/novo")
    public String novo(Model model) {
        // A tela clientes/form espera um objeto chamado "cliente".
        // Mandamos um cliente VAZIO (new Cliente()).
        // A tela mostra o "estado vazio". Na aula 5 esse cliente vem do banco.
        model.addAttribute("cliente", new Cliente());

        // SEM @ResponseBody: o texto devolvido e o nome da tela.
        // Abre: src/main/resources/templates/clientes/form.html
        return "clientes/form";

    }

    @PostMapping("/salvar")
    public String salvar(@ModelAttribute Cliente cliente,
            RedirectAttributes redirect) {

        boolean novo = (cliente.getId() == null);

        repo.save(cliente);

        redirect.addFlashAttribute(
                "mensagem",
                novo ? "Cliente cadastrado com sucesso!"
                        : "Cliente atualizado com sucesso!");

        return "redirect:/clientes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id,
            RedirectAttributes attributes) {
        repo.deleteById(id);
        attributes.addFlashAttribute(
                "mensagem",
                "Cliente excluído com sucesso!");
        return "redirect:/clientes";
    }

    // Abre a tela de edição de um cliente específico
    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {

        // Procura o cliente pelo ID no banco
        Cliente cliente = repo.findById(id).orElseThrow();

        // Envia o cliente encontrado para o formulário
        model.addAttribute("cliente", cliente);

        // Abre o mesmo formulário usado para cadastrar
        return "clientes/form";
    }

}