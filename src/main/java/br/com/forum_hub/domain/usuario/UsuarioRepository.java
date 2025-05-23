package br.com.forum_hub.domain.usuario;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    Optional<Usuario> findByEmailIgnoreCase(String email);
    Optional<Usuario> findByRefreshToken(String refreshToken);
    Optional<Usuario> findByEmailIgnoreCaseOrNomeUsuarioIgnoreCase(String email,
            String nomeUsuario);
    Optional<Usuario> findByToken(String codigo);
    Optional<Usuario> findByNomeUsuario(String nomeUsuario);
}
