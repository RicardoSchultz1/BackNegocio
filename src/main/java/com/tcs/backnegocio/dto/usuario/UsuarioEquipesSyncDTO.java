package com.tcs.backnegocio.dto.usuario;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioEquipesSyncDTO {

    @NotNull
    private Integer usuarioId;

    @NotNull
    private List<Integer> equipeIds;
}
