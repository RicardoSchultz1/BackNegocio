package com.tcs.backnegocio.dto.empresa;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaCreateDTO {

    @NotBlank
    private String nome;

    @NotBlank
    private String cnpj;

    private Integer idAdm;
}