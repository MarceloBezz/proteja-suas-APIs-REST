package br.com.forum_hub.domain.usuario;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.forum_hub.infra.email.EmailService;
import br.com.forum_hub.infra.exception.RegraDeNegocioException;
import jakarta.transaction.Transactional;

@Service
public class UsuarioService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder encoder;
    private final EmailService emailService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder encoder, EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.encoder = encoder;
        this.emailService = emailService;
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

        var senhaCriptografada = encoder.encode(dados.senha());
        var usuario = new Usuario(dados, senhaCriptografada);

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
    public DadosListagemUsuario atualizar(DadosAtualizacaoUsuario dados, String nomeUsuario) {
        var usuario = usuarioRepository.findByNomeUsuario(nomeUsuario)
                .orElseThrow(() -> new RegraDeNegocioException("Usuário não encontrado!"));
        usuario.atualizar(dados);
        return new DadosListagemUsuario(usuario);
    }

    public void atualizarSenha(DadosAtualizacaoSenha dados, Usuario usuario) {
        var senhasIguais = encoder.matches(dados.senhaAntiga(),usuario.getPassword());

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

    public void desativarConta(Usuario usuario) {
        usuario.desativarConta();
        usuarioRepository.save(usuario);
    }

}
