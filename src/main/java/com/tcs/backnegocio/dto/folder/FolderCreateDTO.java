package com.tcs.backnegocio.dto.folder;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderCreateDTO {

    @NotBlank
    private String nome;

    private Integer parentId;

    private Integer equipeId;
    
    private Boolean isRoot;

}
