package br.com.bugnacio.techos.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import br.com.bugnacio.techos.repository.ClienteRepository;

// @Controller: bean do Spring, a recepcao dos pedidos de clientes.
@Controller
// @RequestMapping na classe: o comeco do endereco para TODOS os metodos daqui.
@RequestMapping("/clientes")
public class ClienteController {
    // o controller precisa do repository para acessar o banco de dados.
    private  final ClienteRepository repo;

    // Injeção de dependência: o Spring vai criar um ClienteRepository e passar para o controller.
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
}