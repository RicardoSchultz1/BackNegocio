package com.tcs.backnegocio.dto.ia;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IaSearchDocumentDTO {
    @JsonProperty("document_id")
    private Integer documentId;

    @JsonProperty("arquivo_nome")
    private String arquivoNome;

    @JsonProperty("arquivo_path")
    private String arquivoPath;

    @JsonProperty("max_similarity")
    private Double maxSimilarity;

    @JsonProperty("avg_similarity")
    private Double avgSimilarity;

    @JsonProperty("chunk_count")
    private Integer chunkCount;

    @JsonProperty("download_url")
    private String downloadUrl;
}
