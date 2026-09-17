package com.caseplatform.imageuml.repository;

import com.caseplatform.imageuml.model.EstadoProcesamientoImagen;
import com.caseplatform.imageuml.model.ImagenUML;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio JPA para la persistencia y consulta de imágenes UML procesadas.
 */
@Repository
public interface ImagenUMLRepository extends JpaRepository<ImagenUML, Long> {

    List<ImagenUML> findByUsuarioEmailOrderByFechaCargaDesc(String usuarioEmail);

    List<ImagenUML> findByModeloIdOrderByFechaCargaDesc(Long modeloId);

    List<ImagenUML> findByEstado(EstadoProcesamientoImagen estado);
}
