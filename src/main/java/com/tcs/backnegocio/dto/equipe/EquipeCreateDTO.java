package com.tcs.backnegocio.dto.equipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeCreateDTO {

    @NotBlank
    private String nomeEmpresa;
    @NotNull
    private Integer idAdm;

    private Integer idUser;

    @NotNull
    private Integer idEmpresa;
}