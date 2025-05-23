package br.com.forum_hub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import br.com.forum_hub.domain.usuario.DadosListagemUsuario;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @SuppressWarnings("rawtypes")
    @PostMapping("/registrar")
    public ResponseEntity cadastrar(@RequestBody @Valid DadosCadastroUsuario dados,
            UriComponentsBuilder uriBuilder, @AuthenticationPrincipal Usuario autor) {
        try {
            var usuario = service.cadastrar(dados);
            var uri = uriBuilder.path("/{nomeUsuario}").buildAndExpand(usuario.getNomeUsuario()).toUri();
            return ResponseEntity.created(uri).body(new DadosListagemUsuario(usuario));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/verificar-conta")
    public ResponseEntity<String> verificarEmail(@RequestParam String codigo) {
        try {
            service.verificarEmail(codigo);
            return ResponseEntity.ok("Conta verificada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
