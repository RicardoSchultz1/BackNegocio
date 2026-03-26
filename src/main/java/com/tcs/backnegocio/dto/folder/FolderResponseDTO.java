package com.tcs.backnegocio.dto.folder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderResponseDTO {

    private Integer id;
    private String nome;
    private Integer parentId;
    private Integer equipeId;
    private Boolean isRoot;
    private Boolean deleted;
    private LocalDateTime dataCriacao;
}
