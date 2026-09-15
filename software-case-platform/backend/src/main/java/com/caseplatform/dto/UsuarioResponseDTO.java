package com.caseplatform.dto;

import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para enviar la información del usuario al cliente sin exponer datos sensibles como la contraseña.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long id;
    private String nombreCompleto;
    private String email;
    private Rol rol;
    private EstadoUsuario estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
}
