package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaAlteracaoRequest;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;

import java.util.List;
import java.util.UUID;
public interface TarefaService {
    TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest);
    Tarefa detalhaTarefa(String usuario, UUID idTarefa);
    void concluiTarefa(String usuario, UUID idTarefa);
    void editaTarefa(String email, UUID idTarefa, TarefaAlteracaoRequest tarefaAlteracaoRequest);

    void deletaTodasSuasTarefas(String usuarioEmail, UUID idUsuario);

    List<TarefaListResponse> buscarTodasAsTarefas(String usuario, UUID idUsuario);
    void usuarioModificaOrdemTarefa(String usuario, UUID idTarefa, int novaPosicao);

    void deletaTarefasConcluidas(String usuario, UUID idUsuario);
    void ativaTarefa(String usuario, UUID idTarefa);

    void incrementaPomodoro(String usuarioEmail, UUID idTarefa);
}
