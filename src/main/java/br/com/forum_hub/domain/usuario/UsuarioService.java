package br.com.forum_hub.domain.usuario;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.forum_hub.domain.perfil.DadosPerfil;
import br.com.forum_hub.domain.perfil.PerfilNome;
import br.com.forum_hub.domain.perfil.PerfilRepository;
import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import br.com.forum_hub.infra.seguranca.HierarquiaService;
import jakarta.transaction.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;
    private final PerfilRepository perfilRepository;
    private final HierarquiaService hierarquiaService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encoder, EmailService emailService,
            PerfilRepository perfilRepository, HierarquiaService hierarquiaService) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.emailService = emailService;
        this.perfilRepository = perfilRepository;
        this.hierarquiaService = hierarquiaService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usuarioRepository.findByEmailIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado!"));
    }

    @Transactional
    public Usuario cadastrar(DadosCadastroUsuario dados) {
        Optional<Usuario> optionalUsuario = usuarioRepository
                .findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(dados.email(), dados.nomeUsuario());

        if (optionalUsuario.isPresent()) {
            throw new RegraDeNegocioException("Já existe uma conta cadastrada nesse email ou nome de usuário!");
        }

        var usuario = criarUsuario(dados, false);
        emailService.enviarEmailVerificacao(usuario);
        
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void verificarEmail(String codigo) {
        var usuario = usuarioRepository.findByToken(codigo).orElseThrow();
        usuario.verificar();
    }

    public DadosListagemUsuario pegarUsuario(String nomeUsuario) {
        var usuario = usuarioRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado!"));
        return new DadosListagemUsuario(usuario);
    }

    @Transactional
    public DadosListagemUsuario atualizar(DadosAtualizacaoUsuario dados, Usuario usuario) {
        usuario.atualizar(dados);
        usuarioRepository.save(usuario);
        return new DadosListagemUsuario(usuario);
    }

    public void atualizarSenha(DadosAtualizacaoSenha dados, Usuario usuario) {
        var senhasIguais = encoder.matches(dados.senhaAntiga(), usuario.getPassword());

        if (!senhasIguais) {
            throw new RegraDeNegocioException("Senhas incorreta!");
        }

        if (!dados.novaSenha().equals(dados.confirmacaoNovaSenha())) {
            throw new RegraDeNegocioException("Senhas não são iguais!");
        }

        var senhaCriptografada = encoder.encode(dados.novaSenha());
        usuario.atualizarSenha(senhaCriptografada);
        usuarioRepository.save(usuario);
    }

    public void desativarConta(Long id, Usuario logado) {
        var usuario = usuarioRepository.findById(id).orElseThrow();

        if (hierarquiaService.usuarioNaoTemPermissoes(logado, usuario, "ROLE_ADMIN"))
            throw new RegraDeNegocioException("Você não pode realizar essa ação!");

        if (!usuario.isEnabled())
            throw new RegraDeNegocioException("O usuário já está inativo!");

        usuario.desativarConta();
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario adicionarPerfil(Long id, DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());

        usuario.adicionarPerfil(perfil);
        return usuario;
    }

    @Transactional
    public Usuario removerPerfil(Long id, DadosPerfil dados) {
        var usuario = usuarioRepository.findById(id).orElseThrow();
        var perfil = perfilRepository.findByNome(dados.perfilNome());

        usuario.removerPerfil(perfil);
        return usuario;
    }

    @Transactional
    public void reativarConta(Long id) {
        var usuario = usuarioRepository.findById(id).orElseThrow();

        if (usuario.isEnabled())
            throw new RegraDeNegocioException("O usuário já está ativo!");

        usuario.reativarPerfil();
    }

    @Transactional
    public Usuario cadastrarVerificado(DadosCadastroUsuario dadosUsuario) {
        var usuario = criarUsuario(dadosUsuario, true);
        
        return usuarioRepository.save(usuario);
    }

    private Usuario criarUsuario(DadosCadastroUsuario dados, Boolean verificado) {
        var senhaCriptografada = encoder.encode(dados.senha());

        var perfil = perfilRepository.findByNome(PerfilNome.ESTUDANTE);

        return new Usuario(dados, senhaCriptografada, perfil, verificado);
    }

}
