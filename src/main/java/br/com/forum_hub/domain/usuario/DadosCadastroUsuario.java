package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosCadastroUsuario(
        @NotBlank String email,
        @NotBlank String senha,
        @NotBlank String nomeCompleto,
        @NotBlank String nomeUsuario,
        String miniBiografia,
        String biografia

) {

    public DadosCadastroUsuario(String nomeCompleto2, String nomeUsuario2, String email2, String senha2) {
        this(email2, senha2, nomeCompleto2, nomeUsuario2, null,null);
    }
}