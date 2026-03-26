package com.tcs.backnegocio.dto.folder;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FolderMoveDTO {

    @NotNull
    private Integer folderId;

    @NotNull
    private Integer newParentId;
}
