package br.com.forum_hub.domain.usuario;

import jakarta.validation.constraints.NotBlank;

public record DadosAtualizacaoSenha(
    @NotBlank String senhaAntiga,
    @NotBlank String novaSenha,
    @NotBlank String confirmacaoNovaSenha
) {
    
}
