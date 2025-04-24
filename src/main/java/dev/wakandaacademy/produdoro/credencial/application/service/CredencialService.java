package dev.wakandaacademy.produdoro.credencial.application.service;

import dev.wakandaacademy.produdoro.credencial.domain.Credencial;
import dev.wakandaacademy.produdoro.usuario.application.api.UsuarioNovoRequest;

import javax.validation.Valid;

public interface CredencialService {
	void criaNovaCredencial(@Valid UsuarioNovoRequest usuarioNovo);
	Credencial buscaCredencialPorUsuario(String usuario);
}
