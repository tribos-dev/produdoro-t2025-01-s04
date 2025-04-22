package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import dev.wakandaacademy.produdoro.usuario.infra.UsuarioRepositoryMongoDB;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaIdResponse;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaRequest;
import dev.wakandaacademy.produdoro.tarefa.application.repository.TarefaRepository;
import dev.wakandaacademy.produdoro.tarefa.domain.Tarefa;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class TarefaApplicationServiceTest {

    //	@Autowired
    @InjectMocks
    TarefaApplicationService tarefaApplicationService;

    //	@MockBean
    @Mock
    TarefaRepository tarefaRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }



    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }





    @Test
    void deveAtivarTarefaComSucesso() {
        UUID idTarefa = UUID.randomUUID();
        UUID idUsuario = UUID.randomUUID();
        String emailUsuario = "user@teste.com";

        Tarefa tarefa = new Tarefa(getTarefaRequest());
        tarefa.ativar(); // simula que a tarefa alvo está prestes a ser ativada
        tarefa.inativar(); // forçar estado inicial como inativa

        Usuario usuario = Usuario.builder().idUsuario(idUsuario).email(emailUsuario).build();


        UsuarioRepositoryMongoDB usuarioRepository = null;
        when(usuarioRepository.buscaUsuarioPorEmail(emailUsuario)).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefasDoUsuario(idUsuario)).thenReturn(List.of(tarefa));
        when(tarefaRepository.salva(any())).thenReturn(tarefa);

        tarefaApplicationService.ativaTarefa(emailUsuario, idTarefa);
    }
    
}
