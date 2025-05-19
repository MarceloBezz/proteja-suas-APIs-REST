package br.com.forum_hub.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import br.com.forum_hub.domain.autenticacao.DadosLogin;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.usuario.Usuario;
import jakarta.validation.Valid;

@RestController
public class AutenticacaoController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    
    public AutenticacaoController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("login")
    public ResponseEntity<String> efetuarLogin(@Valid @RequestBody DadosLogin dados) {
        var authenticationToken = new UsernamePasswordAuthenticationToken(dados.email(), dados.senha());
        var authentication = authenticationManager.authenticate(authenticationToken); 

        String tokenAcesso = tokenService.gerarToken((Usuario) authentication.getPrincipal());
        return ResponseEntity.ok(tokenAcesso);
    }
}
