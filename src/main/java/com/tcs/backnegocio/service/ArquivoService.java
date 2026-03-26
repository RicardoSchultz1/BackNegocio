package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import com.tcs.backnegocio.dto.arquivo.ArquivoUploadResponseDTO;
import com.tcs.backnegocio.entity.Arquivo;
import com.tcs.backnegocio.entity.Folder;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.ArquivoRepository;
import com.tcs.backnegocio.repository.FolderRepository;
import com.tcs.backnegocio.storage.SupabaseStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArquivoService {

    private final ArquivoRepository arquivoRepository;
    private final FolderRepository folderRepository;
    private final SupabaseStorageService supabaseStorageService;

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

        String path = supabaseStorageService.upload(file, equipeId, folderId);

        Arquivo arquivo = Arquivo.builder()
                .nome(fileName)
                .path(path)
                .tamanho(file.getSize())
                .tipo(file.getContentType())
                .folder(folder)
                .deleted(false)
                .dataUpload(LocalDateTime.now())
                .build();

        Arquivo saved = arquivoRepository.save(arquivo);

        return ArquivoUploadResponseDTO.builder()
                .id(saved.getId())
                .nome(saved.getNome())
                .path(saved.getPath())
                .tamanho(saved.getTamanho())
                .tipo(saved.getTipo())
                .folderId(saved.getFolder().getId())
                .deleted(saved.getDeleted())
                .dataUpload(saved.getDataUpload())
                .publicUrl(supabaseStorageService.buildPublicUrl(saved.getPath()))
                .build();
    }

    public ArquivoResponseDTO findById(Integer id) {
        Arquivo arquivo = arquivoRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active file not found with id: " + id));

        return toResponse(arquivo);
    }

    public List<ArquivoResponseDTO> findByFolder(Integer folderId) {
        Folder folder = getActiveFolderOrThrow(folderId);

        return arquivoRepository.findByFolderIdAndDeletedFalse(folderId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void softDelete(Integer id) {
        Arquivo arquivo = arquivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

        if (Boolean.TRUE.equals(arquivo.getDeleted())) {
            throw new BusinessException("File is already deleted");
        }

        arquivo.setDeleted(true);
        arquivoRepository.save(arquivo);
    }

    @Transactional
    public void restore(Integer id) {
        Arquivo arquivo = arquivoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("File not found with id: " + id));

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
        return folderRepository.findByIdAndDeletedFalse(folderId)
                .orElseThrow(() -> new ResourceNotFoundException("Active folder not found with id: " + folderId));
    }

    private ArquivoResponseDTO toResponse(Arquivo arquivo) {
        return ArquivoResponseDTO.builder()
                .id(arquivo.getId())
                .nome(arquivo.getNome())
                .path(arquivo.getPath())
                .tamanho(arquivo.getTamanho())
                .tipo(arquivo.getTipo())
                .folderId(arquivo.getFolder().getId())
                .deleted(arquivo.getDeleted())
                .dataUpload(arquivo.getDataUpload())
                .build();
    }
}
