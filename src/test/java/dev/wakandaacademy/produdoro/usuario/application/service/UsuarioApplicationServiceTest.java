package dev.wakandaacademy.produdoro.usuario.application.service;

import ch.qos.logback.classic.spi.ILoggingEvent;
import dev.wakandaacademy.produdoro.DataHelper;
import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.usuario.application.repository.UsuarioRepository;
import dev.wakandaacademy.produdoro.usuario.domain.StatusUsuario;
import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioApplicationServiceTest {
    @InjectMocks
    private UsuarioApplicationService usuarioService;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Test
    void deveMudarStatusParaFocoComSucesso() {
        Usuario usuario = DataHelper.createUsuario();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        usuarioService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario());
        assertEquals(StatusUsuario.FOCO, usuario.getStatus());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());
    }

    @Test
    void naoDeveMudarStatusParaFocoQuandoUsuarioJaEstiverEmFoco() {
        Usuario usuario = DataHelper.createUsuario1();
        when(usuarioRepository.buscaUsuarioPorEmail(usuario.getEmail())).thenReturn(usuario);
        when(usuarioRepository.buscaUsuarioPorId(usuario.getIdUsuario())).thenReturn(usuario);
        APIException exception = assertThrows(APIException.class, () ->
                usuarioService.mudaStatusParaFoco(usuario.getEmail(), usuario.getIdUsuario()));
        assertEquals("O usuário já está em foco.", exception.getMessage());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(usuario.getEmail());

    }

    @Test
    void naoDeveMudarStatusParaFocoQuandoTokeninvalido() {
        Usuario usuarioLogado = DataHelper.createUsuario();
        String emailInvalido = "email@invalido.com";

        when(usuarioRepository.buscaUsuarioPorEmail(emailInvalido))
                .thenThrow( APIException.build(HttpStatus.UNAUTHORIZED,"Credencial de autenticação não é válida"));

        APIException exception = assertThrows(APIException.class,
                () -> usuarioService.mudaStatusParaFoco(emailInvalido, usuarioLogado.getIdUsuario()));

        assertEquals("Credencial de autenticação não é válida", exception.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusException());
        verify(usuarioRepository, times(1)).buscaUsuarioPorEmail(emailInvalido);
        verify(usuarioRepository, never()).buscaUsuarioPorId(any());

    }
}

