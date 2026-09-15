package com.caseplatform.dto;

import com.caseplatform.model.EstadoUsuario;
import com.caseplatform.model.Rol;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    @Size(min = 2, max = 150, message = "El nombre completo debe tener entre 2 y 150 caracteres")
    private String nombreCompleto;

    private Rol rol;

    private EstadoUsuario estado;
}
