package com.caseplatform.repository;

import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import com.caseplatform.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    boolean existsByEmailIgnoreCase(String email);

    List<Usuario> findByRol(Rol rol);

    List<Usuario> findByEstado(EstadoUsuario estado);
}
