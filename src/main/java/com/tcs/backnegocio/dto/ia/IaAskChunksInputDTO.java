package com.tcs.backnegocio.dto.ia;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class IaAskChunksInputDTO {

    private String question;

    @JsonAlias({"document_id", "documentId"})
    private Integer documentId;
}
