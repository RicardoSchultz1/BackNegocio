package com.tcs.backnegocio.dto.folder;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderContentDTO {

    private FolderResponseDTO pasta;
    private List<FolderResponseDTO> subpastas;
    private List<ArquivoResponseDTO> arquivos;
}
