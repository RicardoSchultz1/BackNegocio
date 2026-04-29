package com.tcs.backnegocio.dto.equipe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquipeFuncionariosResponseDTO {

    private Integer idEquipe;
    private List<String> funcionarios;
}
