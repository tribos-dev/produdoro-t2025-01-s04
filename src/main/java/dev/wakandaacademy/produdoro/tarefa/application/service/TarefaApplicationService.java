package dev.wakandaacademy.produdoro.tarefa.application.service;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaListResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class TarefaApplicationService implements TarefaService {
    private final TarefaRepository tarefaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    public TarefaIdResponse criaNovaTarefa(TarefaRequest tarefaRequest) {
        log.info("[inicia] TarefaApplicationService - criaNovaTarefa");
        int posicaoTarefa = tarefaRepository.buscaTarefasDoUsuario(tarefaRequest.getIdUsuario()).size();
        Tarefa tarefaCriada = tarefaRepository.salva(new Tarefa(tarefaRequest, posicaoTarefa));
        log.info("[finaliza] TarefaApplicationService - criaNovaTarefa");
        return TarefaIdResponse.builder().idTarefa(tarefaCriada.getIdTarefa()).build();
    }

    @Override
    public Tarefa detalhaTarefa(String usuario, UUID idTarefa) {
        log.info("[inicia] TarefaApplicationService - detalhaTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        log.info("[usuarioPorEmail] {}", usuarioPorEmail);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "Tarefa não encontrada!"));
        tarefa.pertenceAoUsuario(usuarioPorEmail);
        log.info("[finaliza] TarefaApplicationService - detalhaTarefa");
        return tarefa;
    }

    @Override
    public List<TarefaListResponse> buscarTodasAsTarefas(String usuario, UUID idUsuario) {
        log.info("[inicia] TarefaApplicationService - buscaTodasTarefas");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        usuarioRepository.buscaUsuarioPorId(idUsuario);
        usuarioPorEmail.validaUsuario(idUsuario);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(idUsuario);
        log.info("[finaliza] TarefaApplicationService - buscaTodasTarefas");
        return TarefaListResponse.converte(tarefas);
    }

    @Override
    public void usuarioModificaOrdemTarefa(String usuario, UUID idTarefa, int novaPosicao) {
        log.info("[inicia] TarefaApplicationService - usuarioModificaOrdemTarefa");
        Usuario usuarioPorEmail = usuarioRepository.buscaUsuarioPorEmail(usuario);
        Tarefa tarefa = tarefaRepository.buscaTarefaPorId(idTarefa)
                .orElseThrow(() -> APIException.build(HttpStatus.NOT_FOUND, "ID da tarefa invalido!"));
        tarefa.validaUsuarioETarefa(usuarioPorEmail, idTarefa);
        List<Tarefa> tarefas = tarefaRepository.buscaTarefasDoUsuario(tarefa.getIdUsuario()).stream()
                .sorted(Comparator.comparingInt(Tarefa::getPosicaoTarefa)).collect(Collectors.toList());
        tarefaRepository.modificaOrdemDaTarefa(tarefa, tarefas, novaPosicao);
        tarefa.alteraPosicaoTarefa(novaPosicao);
        tarefaRepository.salva(tarefa);
        log.info("[finaliza] TarefaApplicationService - usuarioModificaOrdemTarefa");
    }
}
