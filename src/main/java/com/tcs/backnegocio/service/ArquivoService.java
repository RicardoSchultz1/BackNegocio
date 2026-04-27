package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoDownloadDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoUploadResponseDTO;
import com.tcs.backnegocio.entity.Arquivo;
import com.tcs.backnegocio.entity.DocumentStatus;
import com.tcs.backnegocio.entity.Folder;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.ArquivoRepository;
import com.tcs.backnegocio.repository.DocumentStatusRepository;
import com.tcs.backnegocio.repository.FolderRepository;
import com.tcs.backnegocio.storage.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    private static final String STATUS_UPLOADED = "UPLOADED";

    private final ArquivoRepository arquivoRepository;
    private final FolderRepository folderRepository;
    private final DocumentStatusRepository documentStatusRepository;
    private final SupabaseStorageService supabaseStorageService;
    private final EquipeAccessService equipeAccessService;

    @Transactional
    public ArquivoUploadResponseDTO upload(Integer folderId, MultipartFile file) {
        Folder folder = getActiveFolderOrThrow(folderId);
        Integer equipeId = folder.getEquipe().getId();

        if (Boolean.TRUE.equals(folder.getDeleted())) {
            throw new BusinessException("Cannot upload into deleted folder");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isBlank()) {
            throw new BusinessException("File name is required");
        }

        if (arquivoRepository.existsActiveByNameAndFolder(fileName, folderId)) {
            throw new BusinessException("File name already exists in this folder", HttpStatus.CONFLICT);
        }

        DocumentStatus uploadedStatus = documentStatusRepository.findByStatusName(STATUS_UPLOADED)
            .orElseThrow(() -> new BusinessException("Document status 'UPLOADED' is not configured", HttpStatus.INTERNAL_SERVER_ERROR));

        String fileHash = sha256Hex((equipeId + ":" + folderId + ":" + fileName + ":" + UUID.randomUUID()).getBytes(StandardCharsets.UTF_8));
        String contentHash = calculateContentHash(file);

        String path = supabaseStorageService.upload(file, equipeId, folderId);

        Arquivo arquivo = Arquivo.builder()
                .nome(fileName)
                .path(path)
            .fileHash(fileHash)
            .contentHash(contentHash)
                .tamanho(file.getSize())
                .tipo(file.getContentType())
                .folder(folder)
            .status(uploadedStatus)
            .totalChunks(0)
                .deleted(false)
                .build();

        Arquivo saved = arquivoRepository.save(arquivo);

        return ArquivoUploadResponseDTO.builder()
                .id(saved.getId())
                .nome(saved.getNome())
                .path(saved.getPath())
                .fileHash(saved.getFileHash())
                .contentHash(saved.getContentHash())
                .tamanho(saved.getTamanho())
                .tipo(saved.getTipo())
                .folderId(saved.getFolder().getId())
                .statusId(saved.getStatus().getId())
                .statusName(saved.getStatus().getStatusName())
                .totalChunks(saved.getTotalChunks())
                .deleted(saved.getDeleted())
                .dataUpload(saved.getDataUpload())
                .updatedAt(saved.getUpdatedAt())
                .publicUrl(supabaseStorageService.buildPublicUrl(saved.getPath()))
                .build();
    }

    public ArquivoResponseDTO findById(Integer id) {
        Arquivo arquivo = getActiveArquivoOrThrow(id);

        return toResponse(arquivo);
    }

        public ArquivoDownloadDTO downloadById(Integer id) {
            Arquivo arquivo = getActiveArquivoOrThrow(id);

        byte[] conteudo = supabaseStorageService.download(arquivo.getPath());

        return ArquivoDownloadDTO.builder()
            .nome(arquivo.getNome())
            .tipo(Objects.requireNonNullElse(arquivo.getTipo(), "application/octet-stream"))
            .tamanho(arquivo.getTamanho())
            .conteudo(conteudo)
            .build();
        }

    public List<ArquivoResponseDTO> findByFolder(Integer folderId) {
        getActiveFolderOrThrow(folderId);

        return arquivoRepository.findByFolderIdAndDeletedFalse(folderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void softDelete(Integer id) {
        Arquivo arquivo = getArquivoOrThrowWithAccess(id);

        if (Boolean.TRUE.equals(arquivo.getDeleted())) {
            throw new BusinessException("File is already deleted");
        }

        arquivo.setDeleted(true);
        arquivoRepository.save(arquivo);
    }

    @Transactional
    public void restore(Integer id) {
        Arquivo arquivo = getArquivoOrThrowWithAccess(id);

        if (!Boolean.TRUE.equals(arquivo.getDeleted())) {
            throw new BusinessException("File is not deleted");
        }

        if (Boolean.TRUE.equals(arquivo.getFolder().getDeleted())) {
            throw new BusinessException("Cannot restore file while folder is deleted");
        }

        if (arquivoRepository.existsActiveByNameAndFolder(arquivo.getNome(), arquivo.getFolder().getId())) {
            throw new BusinessException("A file with same name already exists in this folder", HttpStatus.CONFLICT);
        }

        arquivo.setDeleted(false);
        arquivoRepository.save(arquivo);
    }

    private Folder getActiveFolderOrThrow(Integer folderId) {
        Folder folder = folderRepository.findByIdAndDeletedFalse(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Active folder not found with id: " + folderId));
        equipeAccessService.validateCurrentUserAccess(folder.getEquipe().getId());
        return folder;
    }

    private Arquivo getActiveArquivoOrThrow(Integer id) {
        Arquivo arquivo = arquivoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active file not found with id: " + id));
        equipeAccessService.validateCurrentUserAccess(arquivo.getFolder().getEquipe().getId());
        return arquivo;
    }

    private Arquivo getArquivoOrThrowWithAccess(Integer id) {
        Arquivo arquivo = arquivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));
        equipeAccessService.validateCurrentUserAccess(arquivo.getFolder().getEquipe().getId());
        return arquivo;
    }

    private ArquivoResponseDTO toResponse(Arquivo arquivo) {
        return ArquivoResponseDTO.builder()
                .id(arquivo.getId())
                .nome(arquivo.getNome())
                .path(arquivo.getPath())
                .fileHash(arquivo.getFileHash())
                .contentHash(arquivo.getContentHash())
                .tamanho(arquivo.getTamanho())
                .tipo(arquivo.getTipo())
                .folderId(arquivo.getFolder().getId())
                .statusId(arquivo.getStatus().getId())
                .statusName(arquivo.getStatus().getStatusName())
                .totalChunks(arquivo.getTotalChunks())
                .deleted(arquivo.getDeleted())
                .dataUpload(arquivo.getDataUpload())
                .updatedAt(arquivo.getUpdatedAt())
                .build();
    }

    private String calculateContentHash(MultipartFile file) {
        try {
            return sha256Hex(file.getBytes());
        } catch (IOException e) {
            throw new BusinessException("Failed to calculate content hash");
        }
    }

    private String sha256Hex(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new BusinessException("SHA-256 algorithm is not available", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
