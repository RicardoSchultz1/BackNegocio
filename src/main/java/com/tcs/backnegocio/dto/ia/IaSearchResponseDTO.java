package com.tcs.backnegocio.dto.ia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class IaSearchResponseDTO {
    private List<IaSearchDocumentDTO> documents;
}
