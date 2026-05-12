package com.tcs.backnegocio.dto.usuario;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioUpdateDTO {

    private String nome;

    @Email
    private String email;

    private String senha;

    private Integer idEquipe;

    private List<Integer> idsEquipes;

    private Boolean admSistema;
}
