package br.com.forum_hub.domain.usuario;

import java.util.List;

import br.com.forum_hub.domain.perfil.Perfil;

public record DadosListagemUsuario(Long id,
                                   String email,
                                   String nomeCompleto,
                                   String nomeUsuario,
                                   String miniBiografia,
                                   String biografia,
                                   List<Perfil> perfis
) {
    public DadosListagemUsuario(Usuario usuario) {
        this(usuario.getId(), usuario.getUsername(),
                usuario.getNomeCompleto(), usuario.getNomeUsuario(),
                usuario.getMiniBiografia(), usuario.getBiografia(), usuario.getPerfis());
    }
}