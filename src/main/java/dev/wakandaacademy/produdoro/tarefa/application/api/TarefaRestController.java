    package dev.wakandaacademy.produdoro.tarefa.application.api;

    import dev.wakandaacademy.produdoro.config.security.service.TokenService;
    import dev.wakandaacademy.produdoro.handler.APIException;
    import dev.wakandaacademy.produdoro.tarefa.application.service.TarefaService;
    import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
    import lombok.RequiredArgsConstructor;
    import lombok.extern.log4j.Log4j2;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.List;
    import java.util.UUID;

    @RestController
    @Log4j2
    @RequiredArgsConstructor
    public class TarefaRestController implements TarefaAPI {
        private final TarefaService tarefaService;
        private final TokenService tokenService;

        public TarefaIdResponse postNovaTarefa(TarefaRequest tarefaRequest) {
            log.info("[inicia]  TarefaRestController - postNovaTarefa  ");
            TarefaIdResponse tarefaCriada = tarefaService.criaNovaTarefa(tarefaRequest);
            log.info("[finaliza]  TarefaRestController - postNovaTarefa");
            return tarefaCriada;
        }

        @Override
        public TarefaDetalhadoResponse detalhaTarefa(String token, UUID idTarefa) {
            log.info("[inicia] TarefaRestController - detalhaTarefa");
            String usuario = getUsuarioByToken(token);
            Tarefa tarefa = tarefaService.detalhaTarefa(usuario, idTarefa);
            log.info("[finaliza] TarefaRestController - detalhaTarefa");
            return new TarefaDetalhadoResponse(tarefa);
        }

        @Override
        public void concluiTarefa(String token, UUID idTarefa) {
            log.info("[inicia] TarefaRestController - concluiTarefa");
            String usuarioEmail = getUsuarioByToken(token);
            tarefaService.concluiTarefa(usuarioEmail, idTarefa);
            log.info("[finish] TarefaRestController - concluiTarefa");

        }

        @Override
        public void editaTarefa(String token, TarefaAlteracaoRequest tarefaAlteracaoRequest, UUID idTarefa) {
            log.info("[inicia] TarefaRestController - editaTarefa");
            String email = getUsuarioByToken(token);
            tarefaService.editaTarefa(email, idTarefa, tarefaAlteracaoRequest);
            log.info("[finaliza] TarefaRestController - editaTarefa");
        }

        public void deletaTodasSuasTarefas(String token, UUID idUsuario) {
            log.info("[inicia] TarefaRestController - deletaTodasSuasTarefas");
            String usuarioEmail = getUsuarioByToken(token);
            tarefaService.deletaTodasSuasTarefas(usuarioEmail, idUsuario);
            log.info("[finaliza] TarefaRestController - deletaTodasSuasTarefas");
        }

        @Override
        public List<TarefaListResponse> listarTarefasUsuario(String token, UUID idUsuario) {
            log.info("[inicia] TarefaRestController - listarTarefasUsuario");
            String usuario = getUsuarioByToken(token);
            List<TarefaListResponse> tarefas = tarefaService.buscarTodasAsTarefas(usuario, idUsuario);
            log.info("[finaliza] TarefaRestController - listarTarefasUsuario");
            return tarefas;
        }

    @Override
    public void deletaTarefasConcluidas(String token, UUID idUsuario) {
        log.info("[inicia] TarefaRestController - deletaTarefasConcluidas");
        String usuario = getUsuarioByToken(token);
        tarefaService.deletaTarefasConcluidas(usuario, idUsuario);
        log.info("[finaliza] TarefaRestController - deletaTarefasConcluidas");
    }

    private String getUsuarioByToken(String token) {
        log.debug("[token] {}", token);
        String usuario = tokenService.getUsuarioByBearerToken(token)
                .orElseThrow(() -> APIException.build(HttpStatus.UNAUTHORIZED, token));
        log.info("[usuario] {}", usuario);
        return usuario;
    }
        @Override
        public void incrementaPomodoro(String token, UUID idTarefa) {
            log.info("[inicia] TarefaRestController - incrementaPomodoro");
            String usuarioEmail = getUsuarioByToken(token);
            tarefaService.incrementaPomodoro(usuarioEmail, idTarefa);
            log.info("[finaliza] TarefaRestController - incrementaPomodoro");

        }

        @Override
        public void usuarioModificaOrdemTarefa(String token, UUID idTarefa, int novaPosicao) {
            log.info("[inicia] TarefaRestController - usuarioModificaOrdemTarefa");
            String usuario = getUsuarioByToken(token);
            log.debug("[usuario] {}", usuario);
            tarefaService.usuarioModificaOrdemTarefa(usuario, idTarefa, novaPosicao);
            log.info("[finaliza] TarefaRestController - usuarioModificaOrdemTarefa");
        }


	// Implementação em TarefaRestController ativaTarefa

	@Override
	public void ativaTarefa(String token, UUID idTarefa) {
		log.info("[inicia] TarefaRestController - ativaTarefa");
		String usuario = getUsuarioByToken(token);
		tarefaService.ativaTarefa(usuario, idTarefa);
		log.info("[finaliza] TarefaRestController - ativaTarefa");
	}


}
