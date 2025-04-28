package dev.wakandaacademy.produdoro.tarefa.infra;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Repository
@Log4j2
@RequiredArgsConstructor
public class TarefaInfraRepository implements TarefaRepository {

    private final TarefaSpringMongoDBRepository tarefaSpringMongoDBRepository;

    @Override
    public Tarefa salva(Tarefa tarefa) {
        log.info("[inicia] TarefaInfraRepository - salva");
        try {
            tarefaSpringMongoDBRepository.save(tarefa);
        } catch (DataIntegrityViolationException e) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "Tarefa já cadastrada", e);
        }
        log.info("[finaliza] TarefaInfraRepository - salva");
        return tarefa;
    }

    @Override
    public Optional<Tarefa> buscaTarefaPorId(UUID idTarefa) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefaPorId");
        Optional<Tarefa> tarefaPorId = tarefaSpringMongoDBRepository.findByIdTarefa(idTarefa);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefaPorId");
        return tarefaPorId;
    }

    @Override
    public void deletaTodasSuasTarefas(List<Tarefa> tarefas) {
        log.info("[inicia] TarefaInfraRepository - deletaTodasSuasTarefasPorIdUsuario");
        tarefaSpringMongoDBRepository.deleteAll(tarefas);
        log.info("[finaliza] TarefaInfraRepository - deletaTodasSuasTarefasPorIdUsuario");
    }

    @Override
    public List<Tarefa> buscaTarefasDoUsuario(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasDoUsuario");
        List<Tarefa> todasAsTarefas = tarefaSpringMongoDBRepository.findAllByIdUsuarioOrderByPosicaoTarefaAsc(idUsuario);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasDoUsuario");
        return todasAsTarefas;
    }

    @Override
    public void modificaOrdemDaTarefa(Tarefa tarefasUsuario, List<Tarefa> tarefas, int novaPosicao) {
        log.info("[inicia] TarefaInfraRepository - modificaOrdemDaTarefa");
        if (novaPosicao < 0 || novaPosicao >= tarefas.size()) {
            throw APIException.build(HttpStatus.BAD_REQUEST, "A nova posição da tarefa não é válida!");
        }
        int menorPosicao = (novaPosicao < 0) ? 0 : Math.min(tarefasUsuario.getPosicaoTarefa(), novaPosicao);
        int maiorPosicao = (novaPosicao >= (tarefas.size())) ? tarefas.size() - 1
                : Math.max(tarefasUsuario.getPosicaoTarefa(), novaPosicao);
        validaPosicao(tarefas.size(), tarefasUsuario.getPosicaoTarefa(), novaPosicao);
        novaPosicao = Math.max(0, Math.min(novaPosicao, tarefas.size() - 1));
        salvaTarefas(tarefas, tarefasUsuario.getPosicaoTarefa(), novaPosicao, menorPosicao, maiorPosicao);
        log.info("[finaliza] TarefaInfraRepository - modificaOrdemDaTarefa");

    }

    @Override
    public List<Tarefa> buscaTarefasConcluidas(UUID idUsuario) {
        log.info("[inicia] TarefaInfraRepository - buscaTarefasConcluidas");
        List<Tarefa> tarefasConcluidas = tarefaSpringMongoDBRepository.findAllByIdUsuarioAndStatus(idUsuario, StatusTarefa.CONCLUIDA);
        log.info("[finaliza] TarefaInfraRepository - buscaTarefasConcluidas");
        return tarefasConcluidas;
    }

    @Override
    public void deletaTarefasConcluidas(List<Tarefa> tarefasConcluidas) {
        log.info("[inicia] TarefaInfraRepository - deletaTarefasConcluidas");
        if (tarefasConcluidas.isEmpty()) {
            throw APIException.build(HttpStatus.NOT_FOUND, "Usuário não possui nenhuma tarefa concluída!");
        }
        tarefaSpringMongoDBRepository.deleteAll(tarefasConcluidas);
        log.info("[finaliza] TarefaInfraRepository - deletaTarefasConcluidas");

    }

    private void salvaTarefas(List<Tarefa> tarefas, int origem, int destino, int menorPosicao,
            int maiorPosicao) {
        log.info("[inicia] TarefaInfraRepository - salvaTarefas");
        List<Tarefa> tarefasAtualizadas = IntStream.range(menorPosicao, maiorPosicao)
                .mapToObj(posicaoTarefa -> (Tarefa) novaPosicaoTarefa(tarefas, origem, destino, posicaoTarefa))
                .collect(Collectors.toList());
        log.info("[finaliza] TarefaInfraRepository - salvaTarefas");
        tarefaSpringMongoDBRepository.saveAll(tarefasAtualizadas);

    }

    private Object novaPosicaoTarefa(List<Tarefa> tarefas, int origem, int destino, int posicaoTarefa) {
        log.info("[inicia] TarefaInfraRepository - novaPosicaoTarefa");
        Tarefa tarefa = destino < origem ? atualizaTarefa(tarefas.get(posicaoTarefa), posicaoTarefa + 1)
                : atualizaTarefa(tarefas.get(posicaoTarefa + 1), posicaoTarefa);
        log.info("[finaliza] TarefaInfraRepository - novaPosicaoTarefa");
        return tarefa;
    }

    private Tarefa atualizaTarefa(Tarefa tarefa, int novaPosicao) {
        log.info("[inicia] TarefaInfraRepository - atualizaTarefa");
        tarefa.alteraPosicaoTarefa(novaPosicao);
        log.info("[finaliza] TarefaInfraRepository - atualizaTarefa");
        return tarefa;
    }

    private void validaPosicao(int tamanhoLista, int posicaoDeOrigem, int novaPosicao) {
        log.info("[inicia] TarefaInfraRepository - validaPosicao");
        Optional.of(posicaoDeOrigem)
                .filter(posicaoTarefa -> posicaoTarefa >= 0 && posicaoTarefa < tamanhoLista)
                .orElseThrow(() -> APIException.build(HttpStatus.BAD_REQUEST, "A Posição não é válida!"));
        log.info("[finaliza] TarefaInfraRepository - validaPosicao");

    }
}
