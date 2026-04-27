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
public class ArquivoUploadResponseDTO {

    private Integer id;
    private String nome;
    private String path;
    private String fileHash;
    private String contentHash;
    private Long tamanho;
    private String tipo;
    private Integer folderId;
    private Integer statusId;
    private String statusName;
    private Integer totalChunks;
    private Boolean deleted;
    private LocalDateTime dataUpload;
    private LocalDateTime updatedAt;
    private String publicUrl;
}
