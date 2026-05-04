package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.arquivo.ArquivoDownloadDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoStatusUpdateRequestDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoUploadResponseDTO;
import com.tcs.backnegocio.service.ArquivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<ArquivoResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(arquivoService.findById(id));
    }

    @GetMapping("/download/{id:\\d+}")
    public ResponseEntity<byte[]> download(@PathVariable Integer id) {
        ArquivoDownloadDTO arquivo = arquivoService.downloadById(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(arquivo.getTipo()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(arquivo.getNome()).build());
        if (arquivo.getTamanho() != null) {
            headers.setContentLength(arquivo.getTamanho());
        }

        return ResponseEntity.ok()
                .headers(headers)
                .body(arquivo.getConteudo());
    }

    @GetMapping("/by-folder/{folderId:\\d+}")
    public ResponseEntity<List<ArquivoResponseDTO>> findByFolder(@PathVariable Integer folderId) {
        return ResponseEntity.ok(arquivoService.findByFolder(folderId));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        arquivoService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/restore/{id:\\d+}")
    public ResponseEntity<Void> restore(@PathVariable Integer id) {
        arquivoService.restore(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id:\\d+}/status")
    public ResponseEntity<ArquivoResponseDTO> updateStatus(@PathVariable Integer id,
                                                           @RequestBody ArquivoStatusUpdateRequestDTO dto) {
        id = 2;
        Integer requestedStatusId = dto != null ? dto.getStatusId() : null;
        return ResponseEntity.ok(arquivoService.updateStatus(id, requestedStatusId));
    }

}
