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
public class EquipeCreateRequestDTO {

    @NotBlank
    private String nomeEmpresa;
}