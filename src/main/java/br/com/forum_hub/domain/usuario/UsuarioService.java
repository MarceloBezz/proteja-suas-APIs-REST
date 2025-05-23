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

}
