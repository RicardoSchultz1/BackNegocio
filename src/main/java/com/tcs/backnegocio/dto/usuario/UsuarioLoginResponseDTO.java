package com.tcs.backnegocio.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioLoginResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private Integer idEquipe;
    private Boolean admSistema;
    private String token;
}