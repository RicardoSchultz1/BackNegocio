package com.tcs.backnegocio.dto.equipe;

import jakarta.validation.constraints.NotBlank;
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

    private Integer idUser;
}