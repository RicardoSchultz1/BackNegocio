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
    
    //Mudar para um List<Integer> caso queira atribuir a mais de uma equipe
    @NotNull
    private Integer equipeId;
}
