package com.tcs.backnegocio.dto.arquivo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArquivoStatusUpdateRequestDTO {

    private Integer statusId;
}
