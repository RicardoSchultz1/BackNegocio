package com.tcs.backnegocio.dto.usuario;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEquipeAssignDTO {

    @NotNull
    private Integer usuarioId;

    @NotNull
    private Integer equipeId;
}
