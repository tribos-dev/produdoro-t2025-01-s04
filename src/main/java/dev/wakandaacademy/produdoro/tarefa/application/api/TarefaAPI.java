package dev.wakandaacademy.produdoro.tarefa.application.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/tarefa")
public interface TarefaAPI {
    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    TarefaIdResponse postNovaTarefa(@RequestBody @Valid TarefaRequest tarefaRequest);

    @GetMapping("/{idTarefa}")
    @ResponseStatus(code = HttpStatus.OK)
    TarefaDetalhadoResponse detalhaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idTarefa);

    @PatchMapping("/edita-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void editaTarefa(@RequestHeader(name = "Authorization", required = true) String token,
            @RequestBody @Valid TarefaAlteracaoRequest tarefaAlteracaoRequest,
            @PathVariable UUID idTarefa);

    @PostMapping("/incrementaPomodoro/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void incrementaPomodoro(@RequestHeader(name = "Authorization", required = true) String token,
                            @PathVariable UUID idTarefa);

    @PatchMapping("/modifica-ordem-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void usuarioModificaOrdemTarefa(@RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idTarefa, @RequestParam(required = true, name = "posicao") int novaPosicao);

    @DeleteMapping("/limpar-tarefas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void deletaTodasSuasTarefas(@RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idUsuario);

    @GetMapping("/listarTarefas/{idUsuario}")
    @ResponseStatus(code = HttpStatus.OK)
    List<TarefaListResponse> listarTarefasUsuario(
            @RequestHeader(name = "Authorization", required = true) String token,
            @PathVariable UUID idUsuario);
    @PatchMapping("/conclui-tarefa/{idTarefa}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    void concluiTarefa(@RequestHeader(name = "Authorization", required = true) String token,
                       @PathVariable UUID idTarefa);
}
