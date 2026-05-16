package com.tcs.backnegocio.dto.ia;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class IaAskChunksRequestDTO {

    private String question;

    @JsonProperty("document_id")
    private Integer documentId;

    @JsonProperty("chunk_indexes")
    private List<Integer> chunkIndexes;
}
