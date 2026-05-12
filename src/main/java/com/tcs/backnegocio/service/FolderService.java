package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.arquivo.ArquivoResponseDTO;
import com.tcs.backnegocio.dto.folder.FolderCreateDTO;
import com.tcs.backnegocio.dto.folder.FolderContentDTO;
import com.tcs.backnegocio.dto.folder.FolderMoveDTO;
import com.tcs.backnegocio.dto.folder.FolderResponseDTO;
import com.tcs.backnegocio.dto.folder.FolderSummaryDTO;
import com.tcs.backnegocio.dto.folder.FolderTreeNodeDTO;
import com.tcs.backnegocio.dto.folder.FolderUpdateDTO;
import com.tcs.backnegocio.entity.Arquivo;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.entity.Folder;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.ArquivoRepository;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.FolderFlatProjection;
import com.tcs.backnegocio.repository.FolderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FolderService {

    private static final String ROOT_NAME = "ROOT";

    private final FolderRepository folderRepository;
    private final EquipeRepository equipeRepository;
    private final ArquivoRepository arquivoRepository;
    private final EquipeAccessService equipeAccessService;

    @Transactional
    public FolderResponseDTO create(FolderCreateDTO dto) {
        Folder parent = getParentFolderForCreate(dto.getParentId(), dto.getEquipeId());
        Integer equipeId = parent.getEquipe().getId();
        Integer parentId = parent != null ? parent.getId() : null;

        if (folderRepository.existsActiveByNameAndParentAndEquipe(dto.getNome(), parentId, equipeId)) {
            throw new BusinessException("Folder name already exists in this parent", HttpStatus.CONFLICT);
        }

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + equipeId));

        Folder folder = Folder.builder()
                .nome(dto.getNome())
                .parent(parent)
                .equipe(equipe)
                .isRoot(false)
                .deleted(false)
                .dataCriacao(LocalDateTime.now())
                .build();

        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public FolderResponseDTO createRootFolder(Integer equipeId, String nome) {
        equipeAccessService.validateCurrentUserAccess(equipeId);
        folderRepository.findRootByEquipeId(equipeId)
                .ifPresent(root -> {
                    throw new BusinessException("Root folder already exists for this equipe", HttpStatus.CONFLICT);
                });

        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + equipeId));

        String rootName = (nome == null || nome.isBlank()) ? ROOT_NAME : nome;

        Folder root = Folder.builder()
                .nome(rootName)
                .parent(null)
                .equipe(equipe)
                .isRoot(true)
                .deleted(false)
                .dataCriacao(LocalDateTime.now())
                .build();

        return toResponse(folderRepository.save(root));
    }

    public FolderResponseDTO findById(Integer id) {
        return toResponse(getActiveFolderOrThrow(id));
    }

    public List<FolderSummaryDTO> findRootFolders() {
        List<Integer> equipeIds = equipeAccessService.getAccessibleEquipeIdsForCurrentUser();

        return folderRepository.findRootFoldersByEquipeIds(equipeIds)
                .stream()
                .map(f -> FolderSummaryDTO.builder()
                        .id(f.getId())
                        .nome(f.getNome())
                        .build())
                .toList();
    }

    @Transactional
    public FolderResponseDTO update(Integer id, FolderUpdateDTO dto) {
        Folder folder = getActiveFolderOrThrow(id);

        Integer parentId = folder.getParent() != null ? folder.getParent().getId() : null;
        if (folderRepository.existsActiveByNameAndParentAndEquipe(
                dto.getNome(),
                parentId,
                folder.getEquipe().getId())
                && !dto.getNome().equals(folder.getNome())) {
            throw new BusinessException("Folder name already exists in this parent", HttpStatus.CONFLICT);
        }

        folder.setNome(dto.getNome());
        return toResponse(folderRepository.save(folder));
    }

    public FolderTreeNodeDTO findTree(Integer folderId) {
        getActiveFolderOrThrow(folderId);

        List<FolderFlatProjection> folders = folderRepository.findActiveTree(folderId);
        if (folders.isEmpty()) {
            throw new ResourceNotFoundException("Folder tree not found for folder id: " + folderId);
        }

        List<Arquivo> arquivos = arquivoRepository.findAllActiveInFolderTree(folderId);

        Map<Integer, FolderTreeNodeDTO> nodeById = new HashMap<>();
        FolderTreeNodeDTO rootNode = null;

        for (FolderFlatProjection f : folders) {
            FolderTreeNodeDTO node = FolderTreeNodeDTO.builder()
                    .id(f.getId())
                    .nome(f.getNome())
                    .parentId(f.getParentId())
                    .isRoot(f.getIsRoot())
                    .build();
            nodeById.put(f.getId(), node);
        }

        for (FolderTreeNodeDTO node : nodeById.values()) {
            if (node.getParentId() == null) {
                if (node.getId().equals(folderId)) {
                    rootNode = node;
                }
                continue;
            }

            FolderTreeNodeDTO parent = nodeById.get(node.getParentId());
            if (parent != null) {
                parent.getSubfolders().add(node);
            }

            if (node.getId().equals(folderId)) {
                rootNode = node;
            }
        }

        if (rootNode == null) {
            rootNode = nodeById.get(folderId);
        }

        for (Arquivo arquivo : arquivos) {
            FolderTreeNodeDTO folderNode = nodeById.get(arquivo.getFolder().getId());
            if (folderNode != null) {
                folderNode.getArquivos().add(toArquivoResponse(arquivo));
            }
        }

        sortTree(rootNode);
        return rootNode;
    }

        public FolderContentDTO findContent(Integer folderId) {
        Folder folder = getActiveFolderOrThrow(folderId);

        List<FolderResponseDTO> subpastas = folderRepository.findByParentIdAndDeletedFalse(folderId)
            .stream()
            .map(this::toResponse)
            .sorted(Comparator.comparing(FolderResponseDTO::getNome))
            .toList();

        List<ArquivoResponseDTO> arquivos = arquivoRepository.findByFolderIdAndDeletedFalse(folderId)
            .stream()
            .map(this::toArquivoResponse)
            .sorted(Comparator.comparing(ArquivoResponseDTO::getNome))
            .toList();

        return FolderContentDTO.builder()
            .pasta(toResponse(folder))
            .subpastas(subpastas)
            .arquivos(arquivos)
            .build();
        }

    @Transactional
    public void softDelete(Integer id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));

        if (Boolean.TRUE.equals(folder.getIsRoot())) {
            throw new BusinessException("Root folder cannot be deleted", HttpStatus.BAD_REQUEST);
        }

        if (Boolean.TRUE.equals(folder.getDeleted())) {
            throw new BusinessException("Folder is already deleted");
        }

        folderRepository.markSubtreeDeleted(id, true);
        arquivoRepository.markDeletedByFolderSubtree(id, true);
    }

    @Transactional
    public void restore(Integer id) {
        Folder folder = folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Folder not found with id: " + id));

        if (!Boolean.TRUE.equals(folder.getDeleted())) {
            throw new BusinessException("Folder is not deleted");
        }

        Folder parent = folder.getParent();
        if (parent != null && Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("Cannot restore folder while parent is deleted");
        }

        Integer parentId = parent != null ? parent.getId() : null;
        if (folderRepository.existsActiveByNameAndParentAndEquipe(folder.getNome(), parentId, folder.getEquipe().getId())) {
            throw new BusinessException("A folder with same name already exists in parent", HttpStatus.CONFLICT);
        }

        folderRepository.markSubtreeDeleted(id, false);
        arquivoRepository.markDeletedByFolderSubtree(id, false);
    }

    @Transactional
    public FolderResponseDTO move(FolderMoveDTO dto) {
        Folder folder = getActiveFolderOrThrow(dto.getFolderId());
        Folder newParent = getActiveFolderOrThrow(dto.getNewParentId());

        if (Boolean.TRUE.equals(folder.getIsRoot())) {
            throw new BusinessException("Root folder cannot be moved");
        }

        if (!folder.getEquipe().getId().equals(newParent.getEquipe().getId())) {
            throw new BusinessException("Cannot move folder across equipes");
        }

        if (folder.getId().equals(newParent.getId())) {
            throw new BusinessException("Folder cannot be moved to itself");
        }

        boolean movingInsideOwnTree = folderRepository.isInSubtree(folder.getId(), newParent.getId());
        if (movingInsideOwnTree) {
            throw new BusinessException("Folder cannot be moved into its own subtree");
        }

        if (folderRepository.existsActiveByNameAndParentAndEquipe(
                folder.getNome(),
                newParent.getId(),
                folder.getEquipe().getId())) {
            throw new BusinessException("Folder name already exists in destination", HttpStatus.CONFLICT);
        }

        folder.setParent(newParent);
        return toResponse(folderRepository.save(folder));
    }

    @Transactional
    public Folder ensureRootFolder(Integer equipeId) {
        equipeAccessService.validateCurrentUserAccess(equipeId);
        return ensureRootFolderInternal(equipeId);
    }

    @Transactional
    public Folder ensureRootFolderInternal(Integer equipeId) {
        return folderRepository.findRootByEquipeId(equipeId)
                .orElseGet(() -> {
                    Equipe equipe = equipeRepository.findById(equipeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + equipeId));

                    Folder root = Folder.builder()
                            .nome(equipe.getNomeEmpresa())
                            .parent(null)
                            .equipe(equipe)
                            .isRoot(true)
                            .deleted(false)
                            .dataCriacao(LocalDateTime.now())
                            .build();
                    return folderRepository.save(root);
                });
    }

    @Transactional
    public void syncRootFolderNameWithEquipe(Integer equipeId, String nomeEquipe) {
        folderRepository.findRootByEquipeId(equipeId)
                .ifPresent(root -> {
                    root.setNome(nomeEquipe);
                    folderRepository.save(root);
                });
    }

    private Folder getParentFolderForCreate(Integer parentId, Integer equipeId) {
        if (parentId == null) {
            if (equipeId == null) {
                throw new BusinessException("equipeId is required when parentId is not provided", HttpStatus.BAD_REQUEST);
            }
            return ensureRootFolder(equipeId);
        }

        Folder parent = getActiveFolderOrThrow(parentId);
        if (equipeId != null && !parent.getEquipe().getId().equals(equipeId)) {
            throw new BusinessException("Provided equipeId does not match parent folder equipe", HttpStatus.BAD_REQUEST);
        }
        if (Boolean.TRUE.equals(parent.getDeleted())) {
            throw new BusinessException("Cannot create folder inside a deleted folder");
        }
        return parent;
    }

    private Folder getActiveFolderOrThrow(Integer id) {
        Folder folder = folderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Active folder not found with id: " + id));
        equipeAccessService.validateCurrentUserAccess(folder.getEquipe().getId());
        return folder;
    }

    private FolderResponseDTO toResponse(Folder folder) {
        return FolderResponseDTO.builder()
                .id(folder.getId())
                .nome(folder.getNome())
                .parentId(folder.getParent() != null ? folder.getParent().getId() : null)
                .equipeId(folder.getEquipe().getId())
                .isRoot(folder.getIsRoot())
                .deleted(folder.getDeleted())
                .dataCriacao(folder.getDataCriacao())
                .build();
    }

    private ArquivoResponseDTO toArquivoResponse(Arquivo arquivo) {
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

    private void sortTree(FolderTreeNodeDTO rootNode) {
        if (rootNode == null) {
            return;
        }

        rootNode.getSubfolders().sort(Comparator.comparing(FolderTreeNodeDTO::getNome));
        rootNode.getArquivos().sort(Comparator.comparing(ArquivoResponseDTO::getNome));

        for (FolderTreeNodeDTO child : rootNode.getSubfolders()) {
            sortTree(child);
        }
    }
}
