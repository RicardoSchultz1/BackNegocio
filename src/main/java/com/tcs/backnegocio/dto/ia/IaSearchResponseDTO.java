package com.tcs.backnegocio.dto.ia;

import lombok.Data;
import java.util.List;

@Data
public class IaSearchResponseDTO {
    private List<IaSearchDocumentDTO> documents;
}
