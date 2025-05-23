package br.com.forum_hub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.usuario.DadosAtualizacaoSenha;
import br.com.forum_hub.domain.usuario.DadosAtualizacaoUsuario;
import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;
import br.com.forum_hub.domain.usuario.DadosListagemUsuario;
import br.com.forum_hub.domain.usuario.Usuario;
import br.com.forum_hub.domain.usuario.UsuarioService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

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

    @SuppressWarnings("rawtypes")
    @GetMapping("/{nomeUsuario}")
    public ResponseEntity pegarUsuario(@PathVariable String nomeUsuario) {
        try {
            var usuario = service.pegarUsuario(nomeUsuario);
            return ResponseEntity.ok(usuario);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @PutMapping("/editar-perfil")
    public ResponseEntity atualizarUsuario(@AuthenticationPrincipal Usuario usuario,
            @RequestBody @Valid DadosAtualizacaoUsuario dados) {
        try {
            var usuarioAtualizado = service.atualizar(dados, usuario);
            return ResponseEntity.ok(usuarioAtualizado);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @PatchMapping("/alterar-senha")
    public ResponseEntity atualizarUsuario(@RequestBody @Valid DadosAtualizacaoSenha dados,
            @AuthenticationPrincipal Usuario usuario) {
        try {
            service.atualizarSenha(dados, usuario);
            return ResponseEntity.ok("Senha atualizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/desativar/{id}")
    public ResponseEntity<String> desativarConta(@AuthenticationPrincipal Usuario logado, @PathVariable Long id) {
        try {
            service.desativarConta(id, logado);
            return ResponseEntity.ok("Conta desativada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao desativar conta!");
        }
    }

    @PatchMapping("/reativar/{id}")
    public ResponseEntity<String> reativarConta(@PathVariable Long id) {
        try {
            service.reativarConta(id);
            return ResponseEntity.ok("Conta reativada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Erro ao reativar conta!");
        }
    }

    @SuppressWarnings("rawtypes")
    @PatchMapping("/adicionar-perfil/{id}")
    public ResponseEntity adicionarPerfil(@RequestBody @Valid DadosPerfil dados, @PathVariable Long id) {
        try {
            var usuarioAtualizado = service.adicionarPerfil(id, dados);
            return ResponseEntity.ok(new DadosListagemUsuario(usuarioAtualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
    @PatchMapping("/remover-perfil/{id}")
    public ResponseEntity removerPerfil(@RequestBody @Valid DadosPerfil dados, @PathVariable Long id) {
        try {
            var usuarioAtualizado = service.removerPerfil(id, dados);
            return ResponseEntity.ok(new DadosListagemUsuario(usuarioAtualizado));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
