package br.com.forum_hub.domain.usuario;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import br.com.forum_hub.domain.perfil.Perfil;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
    private boolean ativo;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuarios_perfis", joinColumns = @JoinColumn(name = "usuario_id"), inverseJoinColumns = @JoinColumn(name = "perfil_id"))
    private List<Perfil> perfis = new ArrayList<>();

    public List<Perfil> getPerfis() {
        return perfis;
    }

    public Usuario(String nomeCompleto, String nomeUsuario, String email, String senha, String biografia,
            String miniBiografia) {
        this.nomeCompleto = nomeCompleto;
        this.nomeUsuario = nomeUsuario;
        this.email = email;
        this.senha = senha;
        this.biografia = biografia;
        this.miniBiografia = miniBiografia;
        this.ativo = true;
    }

    @SuppressWarnings("unused")
    private Usuario() {
    }

    public Usuario(DadosCadastroUsuario dados, String senhaCriptografada, Perfil perfil) {
        this.nomeCompleto = dados.nomeCompleto();
        this.nomeUsuario = dados.nomeUsuario();
        this.email = dados.email();
        this.senha = senhaCriptografada;
        this.biografia = dados.biografia();
        this.miniBiografia = dados.miniBiografia();
        this.verificado = false;
        this.token = UUID.randomUUID().toString();
        this.expiracaoToken = LocalDateTime.now().plusMinutes(30);
        this.ativo = true;
        this.perfis.add(perfil);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return perfis;
    }

    @Override
    public boolean isEnabled() {
        return verificado && ativo;
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

    public void atualizar(DadosAtualizacaoUsuario dados) {
        if (dados.nomeCompleto() != null && dados.nomeCompleto() != "") {
            this.nomeCompleto = dados.nomeCompleto();
        }

        if (dados.nomeUsuario() != null && dados.nomeUsuario() != "") {
            this.nomeUsuario = dados.nomeUsuario();
        }

        if (dados.miniBiografia() != null && dados.miniBiografia() != "") {
            this.miniBiografia = dados.miniBiografia();
        }

        if (dados.biografia() != null && dados.biografia() != "") {
            this.biografia = dados.biografia();
        }
    }

    public void atualizarSenha(String senhaCriptografada) {
        this.senha = senhaCriptografada;
    }

    public void desativarConta() {
        this.ativo = false;
    }

    public void adicionarPerfil(Perfil perfil) {
        this.perfis.add(perfil);
    }

    public void removerPerfil(Perfil perfil) {
        this.perfis.remove(perfil);
    }

    public void reativarPerfil() {
        this.ativo = true;
    }
}
