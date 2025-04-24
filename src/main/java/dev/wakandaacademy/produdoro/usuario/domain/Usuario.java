package dev.wakandaacademy.produdoro.usuario.domain;

import dev.wakandaacademy.produdoro.handler.APIException;
import dev.wakandaacademy.produdoro.pomodoro.domain.ConfiguracaoPadrao;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.Email;
import java.util.UUID;

@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@ToString
@Document(collection = "Usuario")
public class Usuario {
	@Id
	private UUID idUsuario;
	@Email
	@Indexed(unique = true)
	private String email;
	private ConfiguracaoUsuario configuracao;
	@Builder.Default
	private StatusUsuario status = StatusUsuario.FOCO;
	@Builder.Default
	private Integer quantidadePomodorosPausaCurta = 0;
	
	public Usuario(UsuarioNovoRequest usuarioNovo, ConfiguracaoPadrao configuracaoPadrao) {
		this.idUsuario = UUID.randomUUID();
		this.email = usuarioNovo.getEmail();
		this.status = StatusUsuario.FOCO;
		this.configuracao = new ConfiguracaoUsuario(configuracaoPadrao);
	}

    public void mudaStatusParaPausaLonga(UUID idUsuario) {
		validaUsuario(idUsuario);
		validaStatusPausaLonga();
		mudaStatusPausaLonga();
    }

	private void mudaStatusPausaLonga() {
		this.status = StatusUsuario.PAUSA_LONGA;
	}

	private void validaStatusPausaLonga() {
		if (this.status.equals(StatusUsuario.PAUSA_LONGA)) {
			throw APIException.build(HttpStatus.CONFLICT, "Usuário já está em pausa longa");
		}
	}

	public void validaUsuario(UUID idUsuario) {
		if (!this.idUsuario.equals(idUsuario)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuário(a) não autorizado(a) para a requisição solicitada");
		}
	}

    public void validaIdUsuario(UUID idUsuario) {
		if (!this.idUsuario.equals(idUsuario)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, "Usuario nao autorizado para a requisição solicitada!");

		}
    }

    public void mudaStatusParaFoco(UUID idUsuario) {
		perteceAoUsuario(idUsuario);
		verificaStatusFoco();
		alteraStatusFoco();
    }

	private void alteraStatusFoco() {
		this.status = StatusUsuario.FOCO;
	}

	private void verificaStatusFoco() {
		if (this.status.equals(StatusUsuario.FOCO)) {
			throw APIException.build(HttpStatus.CONFLICT, "O usuário já está em foco.");
		}

	}

	private void perteceAoUsuario(UUID usuarioFoco) {
		if (!this.idUsuario.equals(usuarioFoco)) {
			throw APIException.build(HttpStatus.UNAUTHORIZED, " credencial de autenticação não é válida. ");
		}
	}
}
