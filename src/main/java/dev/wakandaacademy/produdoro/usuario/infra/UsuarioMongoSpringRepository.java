package dev.wakandaacademy.produdoro.usuario.infra;

import dev.wakandaacademy.produdoro.usuario.domain.Usuario;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;
import java.util.UUID;

public interface UsuarioMongoSpringRepository extends MongoRepository<Usuario, UUID> {
    Optional<Usuario> findByIdUsuario(UUID idUsuario);
    Optional<Usuario> findByEmail(String email);
}
