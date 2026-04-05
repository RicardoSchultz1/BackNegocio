package com.tcs.backnegocio.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLoginResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private Integer idEquipe;
    private List<Integer> idsEquipes;
    private Boolean admSistema;
    private String token;
}