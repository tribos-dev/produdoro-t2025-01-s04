package dev.wakandaacademy.produdoro.tarefa.application.api;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class TarefaAlteracaoRequest {

    @NotBlank
    @Size(message = "o campo não pode estar vazio", max = 255, min = 3)
    private String descricao;

    public TarefaAlteracaoRequest(String descricao) {
        this.descricao = descricao;
    }
}
