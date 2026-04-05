package com.tcs.backnegocio.dto.arquivo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoDownloadDTO {

    private String nome;
    private String tipo;
    private Long tamanho;
    private byte[] conteudo;
}
