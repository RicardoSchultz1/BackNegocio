package com.tcs.backnegocio.dto.arquivo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoResponseDTO {

    private Integer id;
    private String nome;
    private String path;
    private Long tamanho;
    private String tipo;
    private Integer folderId;
    private Boolean deleted;
    private LocalDateTime dataUpload;
}
