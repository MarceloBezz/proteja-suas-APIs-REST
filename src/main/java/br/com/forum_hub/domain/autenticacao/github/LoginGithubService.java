package br.com.forum_hub.domain.autenticacao.github;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import br.com.forum_hub.domain.usuario.DadosCadastroUsuario;

@Service
public class LoginGithubService {

    private final String clientIdLogin = "Ov23liXmFPojLv42Wd6t";
    private final String clientSecretLogin = "e8b23c09c444c7d076d1b553396a9e11bd1e8a4a";
    private final String clientIdCadastro = "Ov23liMMr0dod8CnfG5X";
    private final String clientSecretCadastro = "cfbfd86f3eaf8e20341bed0afc4cce36287cf505";
    private final String redirectUriLogin = "http://localhost:8080/login/github/autorizado";
    private final String redirectUriCadastro = "http://localhost:8080/registro/github/autorizado";
    private final RestClient restClient;

    public LoginGithubService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public String gerarUrlLogin() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientIdLogin +
                "&redirect_uri=" + redirectUriLogin +
                "&scope=user:email,public_repo";
    }

    public String gerarUrlCadastro() {
        return "https://github.com/login/oauth/authorize" +
                "?client_id=" + clientIdCadastro +
                "&redirect_uri=" + redirectUriCadastro +
                "&scope=read:user,user:email";
    }

    public String obterEmail(String code) {
        var token = obterToken(code, clientIdLogin, clientSecretLogin, redirectUriLogin);

        var headers = new HttpHeaders();
        headers.setBearerAuth(token);

        return enviarRequisicaoEmail(headers);
    }

    public DadosCadastroUsuario obterDadosOAuth(String codigo) {
        var accessToken = obterToken(codigo, clientIdCadastro, clientSecretCadastro, redirectUriCadastro);

        var headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        var email = enviarRequisicaoEmail(headers);

        var resposta = restClient.get()
                .uri("https://api.github.com/user")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(Map.class);

        var nomeCompleto = resposta.get("name").toString();
        var nomeUsuario = resposta.get("login").toString();

        var senha = UUID.randomUUID().toString();

        return new DadosCadastroUsuario(nomeCompleto, nomeUsuario, email, senha);
    }

    private String enviarRequisicaoEmail(HttpHeaders headers) {
        var resposta = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(httpHeaders -> httpHeaders.addAll(headers))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(DadosEmail[].class);

        for (DadosEmail d : resposta) {
            if (d.primary() && d.verified()) {
                return d.email();
            }
        }

        return null;
    }

    private String obterToken(String code, String id, String secret, String uri) {
        var resposta = restClient
                .post()
                .uri("https://github.com/login/oauth/access_token")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of("code", code, "client_id", id,
                        "client_secret", secret, "redirect_uri", uri))
                .retrieve()
                .body(Map.class);

        return resposta.get("access_token").toString();
    }
}
