package com.tcs.backnegocio.dto.empresa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmpresaResponseDTO {

    private Integer id;
    private String nome;
    private String cnpj;
    private LocalDate dataCadastro;
    private Integer idAdm;
}