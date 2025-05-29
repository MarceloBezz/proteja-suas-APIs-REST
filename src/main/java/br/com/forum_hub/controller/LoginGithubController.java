package br.com.forum_hub.controller;

import org.springframework.web.bind.annotation.RestController;

import br.com.forum_hub.domain.autenticacao.DadosToken;
import br.com.forum_hub.domain.autenticacao.TokenService;
import br.com.forum_hub.domain.autenticacao.github.LoginGithubService;
import br.com.forum_hub.domain.usuario.UsuarioRepository;
import br.com.forum_hub.domain.usuario.UsuarioService;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
public class LoginGithubController {

    private final LoginGithubService loginGithubService;
    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final UsuarioService usuarioService;

    public LoginGithubController(LoginGithubService loginGithubService, UsuarioRepository usuarioRepository, TokenService tokenService, UsuarioService usuarioService) {
        this.loginGithubService = loginGithubService;
        this.usuarioRepository = usuarioRepository;
        this.tokenService = tokenService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login/github")
    public ResponseEntity<Void> redirecionarGithub() {
        // url
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(loginGithubService.gerarUrlLogin()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/login/github/autorizado")
    public ResponseEntity<DadosToken> autenticarUsuarioOAuth(@RequestParam String code) {        
        var email = loginGithubService.obterEmail(code);

        var usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .orElseThrow();
        var authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = usuario.novoRefreshToken();

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken));
    }

    @GetMapping("/registro/github")
    public ResponseEntity<Void> redirecionarGithubRegistro() {
        var headers = new HttpHeaders();
        headers.setLocation(URI.create(loginGithubService.gerarUrlCadastro()));

        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/registro/github/autorizado")
    public ResponseEntity<DadosToken> autenticarCadastroOAuth(@RequestParam String code) {
        var dadosUsuario = loginGithubService.obterDadosOAuth(code);
        var usuario = usuarioService.cadastrarVerificado(dadosUsuario);

        var authentication = new UsernamePasswordAuthenticationToken(usuario, null,
                usuario.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String tokenAcesso = tokenService.gerarToken(usuario);
        String refreshToken = usuario.novoRefreshToken();

        return ResponseEntity.ok(new DadosToken(tokenAcesso, refreshToken)); 
    }
    

}
