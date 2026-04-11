package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.folder.FolderCreateDTO;
import com.tcs.backnegocio.dto.folder.FolderContentDTO;
import com.tcs.backnegocio.dto.folder.FolderMoveDTO;
import com.tcs.backnegocio.dto.folder.FolderResponseDTO;
import com.tcs.backnegocio.dto.folder.FolderSummaryDTO;
import com.tcs.backnegocio.dto.folder.FolderTreeNodeDTO;
import com.tcs.backnegocio.service.FolderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/folders")
@RequiredArgsConstructor
public class FolderController {

    private final FolderService folderService;

    @GetMapping("/roots")
    public ResponseEntity<List<FolderSummaryDTO>> findRootFolders() {
        return ResponseEntity.ok(folderService.findRootFolders());
    }

    @PostMapping("/create")
    public ResponseEntity<FolderResponseDTO> create(@Valid @RequestBody FolderCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(folderService.create(dto));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<FolderResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(folderService.findById(id));
    }

    @GetMapping("/tree/{folderId:\\d+}")
    public ResponseEntity<FolderTreeNodeDTO> findTree(@PathVariable Integer folderId) {
        return ResponseEntity.ok(folderService.findTree(folderId));
    }

    @GetMapping("/content/{folderId:\\d+}")
    public ResponseEntity<FolderContentDTO> findContent(@PathVariable Integer folderId) {
        return ResponseEntity.ok(folderService.findContent(folderId));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        folderService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/{id:\\d+}")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        folderService.restore(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/move")
    public ResponseEntity<FolderResponseDTO> move(@Valid @RequestBody FolderMoveDTO dto) {
        return ResponseEntity.ok(folderService.move(dto));
    }
}
