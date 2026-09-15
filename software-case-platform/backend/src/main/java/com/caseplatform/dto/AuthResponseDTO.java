package com.caseplatform.dto;

import com.caseplatform.model.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para login exitoso que entrega el token JWT y los datos esenciales del usuario.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {

    private String token;
    private UsuarioResponseDTO usuario;
    private Rol rol;
    @Builder.Default
    private String tipoToken = "Bearer";
}
