package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoUploadResponseDTO;
import com.tcs.backnegocio.service.ArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/arquivos")
@RequiredArgsConstructor
public class ArquivoController {

    private final ArquivoService arquivoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ArquivoUploadResponseDTO> upload(@RequestParam("folderId") Integer folderId,
                                                           @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(arquivoService.upload(folderId, file));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArquivoResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(arquivoService.findById(id));
    }

    @GetMapping("/by-folder/{folderId}")
    public ResponseEntity<List<ArquivoResponseDTO>> findByFolder(@PathVariable Integer folderId) {
        return ResponseEntity.ok(arquivoService.findByFolder(folderId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        arquivoService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        arquivoService.restore(id);
        return ResponseEntity.ok().build();
    }
}
