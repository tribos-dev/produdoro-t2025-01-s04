package dev.wakandaacademy.produdoro.tarefa.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.*;

import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.tarefa.application.api.TarefaAlteracaoRequest;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
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

    @Mock
    UsuarioRepository usuarioRepository;

    @Test
    void deveRetornarIdTarefaNovaCriada() {
        TarefaRequest request = getTarefaRequest();
        when(tarefaRepository.salva(any())).thenReturn(new Tarefa(request, 0));

        TarefaIdResponse response = tarefaApplicationService.criaNovaTarefa(request);

        assertNotNull(response);
        assertEquals(TarefaIdResponse.class, response.getClass());
        assertEquals(UUID.class, response.getIdTarefa().getClass());
    }

    @Test
    void deveDeletarTodasTarefas() {
        Usuario usuario = DataHelper.createUsuario();
        List<Tarefa> tarefas = DataHelper.createListTarefa();
        String emailUsuario = usuario.getEmail();
        UUID idUsuario = usuario.getIdUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasDoUsuario(any())).thenReturn(tarefas);
        tarefaApplicationService.deletaTodasSuasTarefas(emailUsuario, idUsuario);
        verify(tarefaRepository, times(1)).deletaTodasSuasTarefas(tarefas);
    }

    public TarefaRequest getTarefaRequest() {
        TarefaRequest request = new TarefaRequest("tarefa 1", UUID.randomUUID(), null, null, 0);
        return request;
    }

    @Test
    void deveAlterarTarefa() {
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        TarefaAlteracaoRequest tarefaAlteracaoRequest = DataHelper.createEditaTarefa();
        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        tarefaApplicationService.editaTarefa(usuario.getEmail(), tarefa.getIdTarefa(), tarefaAlteracaoRequest);
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
        assertEquals("Tarefa 2", tarefa.getDescricao());
    }

    @Test
    void naoDeveAlterarTarefa() {
        UUID idTarefaInvalida = UUID.randomUUID();
        String usuario = "joao";
        TarefaAlteracaoRequest tarefaAlteracaoRequest = DataHelper.createEditaTarefa();
        when(tarefaRepository.buscaTarefaPorId(idTarefaInvalida)).thenReturn(Optional.empty());
        assertThrows(APIException.class,
                () -> tarefaApplicationService.editaTarefa(usuario, idTarefaInvalida, tarefaAlteracaoRequest));
        Optional<Tarefa> tarefa = verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefaInvalida);
    }

    @Test
    void deveModificarOrdemDaTarefaComSucesso() {
        // Cenário positivo
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = DataHelper.createTarefa();
        List<Tarefa> tarefas = DataHelper.createListTarefa();
        int novaPosicao = 2;

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));
        when(tarefaRepository.buscaTarefasDoUsuario(any())).thenReturn(tarefas);

        assertDoesNotThrow(() -> tarefaApplicationService.usuarioModificaOrdemTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao));

        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
        verify(tarefaRepository, times(1)).buscaTarefasDoUsuario(tarefa.getIdUsuario());
        verify(tarefaRepository, times(1)).modificaOrdemDaTarefa(tarefa, tarefas, novaPosicao);
        verify(tarefaRepository, times(1)).salva(tarefa);
    }

    @Test
    void naoDeveModificarOrdemQuandoUsuarioNaoAutorizado() {
        // Cenário negativo: Usuário não autorizado
        Usuario usuario = DataHelper.createUsuario();
        Tarefa tarefa = Tarefa.builder().idUsuario(UUID.randomUUID()).build(); // Tarefa com outro usuário
        int novaPosicao = 2;

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(any())).thenReturn(Optional.of(tarefa));

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.usuarioModificaOrdemTarefa(usuario.getEmail(), tarefa.getIdTarefa(), novaPosicao));

        assertEquals("Usuário(a) não autorizado(a) para a requisição solicitada!", exception.getBodyException().getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(tarefa.getIdTarefa());
        verify(tarefaRepository, never()).modificaOrdemDaTarefa(any(), any(), anyInt());
    }

    @Test
    void naoDeveModificarOrdemQuandoTarefaNaoEncontrada() {
        // Cenário negativo: Tarefa não encontrada
        Usuario usuario = DataHelper.createUsuario();
        UUID idTarefaInvalida = UUID.randomUUID();
        int novaPosicao = 2;

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefaPorId(idTarefaInvalida)).thenReturn(Optional.empty());

        APIException exception = assertThrows(APIException.class,
                () -> tarefaApplicationService.usuarioModificaOrdemTarefa(usuario.getEmail(), idTarefaInvalida, novaPosicao));

        assertEquals("ID da tarefa invalido!", exception.getBodyException().getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
        verify(tarefaRepository, times(1)).buscaTarefaPorId(idTarefaInvalida);
        verify(tarefaRepository, never()).modificaOrdemDaTarefa(any(), any(), anyInt());
    }

    @Test
    void deveExcluirTarefasConcluidasComSucesso() {
        Usuario usuario = DataHelper.createUsuario1();
        List<Tarefa> tarefasConcluidas = DataHelper.createListTarefa();
        UUID idUsuario = usuario.getIdUsuario();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);
        when(tarefaRepository.buscaTarefasConcluidas(idUsuario)).thenReturn(tarefasConcluidas);

        assertDoesNotThrow(() -> tarefaApplicationService.deletaTarefasConcluidas(usuario.getEmail(), idUsuario));

        verify(tarefaRepository, times(1)).deletaTarefasConcluidas(tarefasConcluidas);
    }

    @Test
    void deveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        Usuario usuario = DataHelper.createUsuario();
        UUID idUsuario = UUID.randomUUID();
        String usuarioEmail = usuario.getEmail();

        when(usuarioRepository.buscaUsuarioPorEmail(any())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(any())).thenReturn(usuario);

        assertThrows(APIException.class,() -> tarefaApplicationService.deletaTarefasConcluidas(usuarioEmail, idUsuario));
    }
}
