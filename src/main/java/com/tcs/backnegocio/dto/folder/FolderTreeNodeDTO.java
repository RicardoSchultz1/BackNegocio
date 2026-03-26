package com.tcs.backnegocio.dto.folder;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderTreeNodeDTO {

    private Integer id;
    private String nome;
    private Integer parentId;
    private Boolean isRoot;

    @Builder.Default
    private List<FolderTreeNodeDTO> subfolders = new ArrayList<>();

    @Builder.Default
    private List<ArquivoResponseDTO> arquivos = new ArrayList<>();
}
