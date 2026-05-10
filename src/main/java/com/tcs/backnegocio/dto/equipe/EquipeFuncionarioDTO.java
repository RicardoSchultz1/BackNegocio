package com.tcs.backnegocio.dto.equipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeFuncionarioDTO {

    private Integer id;
    private String nome;
}