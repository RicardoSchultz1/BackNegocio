package com.tcs.backnegocio.dto.usuario;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Integer id;
    private String nome;
    private String email;
    private LocalDate dataCadastro;
    private Integer idEquipe;
    private List<Integer> idsEquipes;
    private Boolean admSistema;
}