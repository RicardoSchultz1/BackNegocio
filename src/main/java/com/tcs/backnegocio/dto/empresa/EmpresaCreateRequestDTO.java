package com.tcs.backnegocio.dto.empresa;

import com.tcs.backnegocio.dto.equipe.EquipeCreateRequestDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioCreateDTO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaCreateRequestDTO {

    @Valid
    @NotNull
    private EmpresaCreateDTO empresa;

    @Valid
    @NotNull
    private UsuarioCreateDTO usuario;

    //@Valid
    //@NotNull
    //private EquipeCreateRequestDTO equipe;
}