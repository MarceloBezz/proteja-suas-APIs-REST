package br.com.forum_hub.domain.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.forum_hub.infra.exception.RegraDeNegocioException;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCompleto;
    private String nomeUsuario;
    private String email;
    private String senha;
    private String biografia;
    private String miniBiografia;
    private String refreshToken;
    private LocalDateTime expiracaoRefreshToken;
    private boolean verificado;
    private String token;
    private LocalDateTime expiracaoToken;

    public Usuario(String nomeCompleto, String nomeUsuario, String email, String senha, String biografia,
            String miniBiografia) {
        this.nomeCompleto = nomeCompleto;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.senha = senha;
        this.biografia = biografia;
        this.miniBiografia = miniBiografia;
    }

    @SuppressWarnings("unused")
    private Usuario() {
    }

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada) {
        this.nomeCompleto = dados.nomeCompleto();
        this.nomeUsuario = dados.nomeUsuario();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();
        this.verificado = false;
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return verificado;
    }

    @Override
    public String getPassword() {
        return senha;
    }

    @Override
    public String getUsername() {
        return email;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getBiografia() {
        return biografia;
    }

    public String getMiniBiografia() {
        return miniBiografia;
    }

    public Long getId() {
        return id;
    }

    public boolean isRefreshTokenExpirado() {
        return expiracaoRefreshToken.isBefore(LocalDateTime.now());
    }

    public String novoRefreshToken() {
        this.refreshToken = UUID.randomUUID().toString();
        this.expiracaoRefreshToken = LocalDateTime.now().plusMinutes(120);
        return refreshToken;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpiracaoToken() {
        return expiracaoToken;
    }

    public void verificar() {
        if (!expiracaoToken.isBefore(LocalDateTime.now())) {
            this.verificado = true;
            this.token = null;
            this.expiracaoToken = null;
        } else {
            throw new RegraDeNegocioException("Token expirado!");
        }
    }
}
