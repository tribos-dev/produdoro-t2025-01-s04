package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.autenticacao.domain.Token;
import dev.wakandaacademy.produdoro.config.security.service.TokenService;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusAtivacaoTarefa;
import dev.wakandaacademy.produdoro.tarefa.domain.StatusTarefa;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import dev.wakandaacademy.produdoro.usuario.infra.UsuarioRepositoryMongoDB;
import org.aspectj.weaver.patterns.ITokenSource;
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

    //	@MockBean
    @Mock
    UsuarioRepository usuarioRepository;

    @Mock
    TokenService tokenService;

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

        Tarefa tarefa = DataHelper.createTarefa();

        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefasDoUsuario(usuario.getIdUsuario())).thenReturn(List.of(tarefa));
        when(tarefaRepository.salva(any())).thenReturn(tarefa);

        tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefa.getIdTarefa());

        assertEquals(StatusAtivacaoTarefa.ATIVA , tarefa.getStatusAtivacao());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    }


    @Test
    void naoDeveAtivarTarefaQueJaEstaAtiva() {
        Tarefa tarefa = DataHelper.createTarefaAtiva();

        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.ativaTarefa(usuario.getEmail(), tarefa.getIdTarefa()));

      assertEquals(HttpStatus.CONFLICT, exception.getStatusException());
        assertEquals("Tarefa já está ativa!", exception.getMessage());
    }


    @Test
    void deveLancarExcecaoQuandoIdTarefaForInvalido() {
        UUID idTarefa = UUID.randomUUID();


        Usuario usuario = DataHelper.createUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefa)).thenReturn(Optional.empty());


        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.ativaTarefa(usuario.getEmail(),idTarefa));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusException());
        assertEquals("Id da tarefa inválido.", exception.getMessage());
    }


    @Test
    void deveLancarExcecaoQuandoTarefaNaoPertenceAoUsuario() {
        Tarefa tarefa = DataHelper.createTarefa();

        Usuario usuario = DataHelper.createUsuarioDiferente();

        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(tarefa.getIdTarefa())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.ativaTarefa(usuario.getEmail(),tarefa.getIdTarefa()));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Usuário não é dono da Tarefa solicitada!", exception.getMessage());
    }


    @Test
    void deveLancarExcecaoQuandoUsuarioNaoForEncontrado() {
        String emailUsuario = "usuario.inexistente@teste.com";

        when(tokenService.getUsuarioByBearerToken(emailUsuario))
                .thenThrow(APIException.build(HttpStatus.UNAUTHORIZED, "Token inválido"));

        APIException exception = assertThrows(APIException.class,
                () -> tokenService.getUsuarioByBearerToken(emailUsuario));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        assertEquals("Token inválido", exception.getMessage());
    }


}
