package com.tcs.backnegocio.dto.equipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeResponseDTO {

    private Integer id;
    private String nomeEmpresa;
    private Integer idAdm;
    private Integer idUser;
    private Integer idEmpresa;
}