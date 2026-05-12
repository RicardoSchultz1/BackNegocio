package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.equipe.EquipeCreateDTO;
import com.tcs.backnegocio.dto.equipe.EquipeFuncionarioDTO;
import com.tcs.backnegocio.dto.equipe.EquipeFuncionariosResponseDTO;
import com.tcs.backnegocio.dto.equipe.EquipeResponseDTO;
import com.tcs.backnegocio.dto.equipe.EquipeUpdateDTO;
import com.tcs.backnegocio.entity.Empresa;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.EmpresaRepository;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EmpresaRepository empresaRepository;
    private final FolderService folderService;
    private final EquipeAccessService equipeAccessService;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EquipeResponseDTO create(EquipeCreateDTO dto) {
        Usuario adm = equipeAccessService.getAuthenticatedUsuarioOrThrow();

        Empresa empresa = adm.getEquipes().stream()
                .map(Equipe::getEmpresa)
                .filter(e -> e != null)
                .findFirst()
                .orElseThrow(() -> new BusinessException("Authenticated user has no empresa associated"));

        Equipe equipe = Equipe.builder()
                .nomeEmpresa(dto.getNomeEmpresa())
                .idAdm(adm.getId())
                .idUser(dto.getIdUser())
                .empresa(empresa)
                .build();

        Equipe saved = equipeRepository.save(equipe);

        // Usuario is the owning side of usuario_equipe; persist links from user to equipe.
        adm.getEquipes().add(saved);
        usuarioRepository.save(adm);

        if (dto.getIdUser() != null && !dto.getIdUser().equals(adm.getId())) {
            Usuario usuario = usuarioRepository.findWithEquipesByIdAndAtivoTrue(dto.getIdUser())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario not found with id: " + dto.getIdUser()));
            usuario.getEquipes().add(saved);
            usuarioRepository.save(usuario);
        }

        folderService.ensureRootFolderInternal(saved.getId());
        return toResponseDTO(saved);
    }

    public EquipeResponseDTO findById(Integer id) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + id));

        return toResponseDTO(equipe);
    }

    public List<EquipeResponseDTO> findAll() {
        return equipeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional
    public EquipeResponseDTO update(Integer id, EquipeUpdateDTO dto) {
        Equipe equipe = equipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + id));

        equipeAccessService.validateCurrentUserAccess(equipe.getId());
        equipe.setNomeEmpresa(dto.getNomeEmpresa());
        Equipe saved = equipeRepository.save(equipe);

        folderService.syncRootFolderNameWithEquipe(saved.getId(), saved.getNomeEmpresa());
        return toResponseDTO(saved);
    }

    public EquipeFuncionariosResponseDTO findWorkerNamesByEquipeId(Integer equipeId) {
        if (!equipeRepository.existsById(equipeId)) {
            throw new ResourceNotFoundException("Equipe not found with id: " + equipeId);
        }

        equipeAccessService.validateCurrentUserAccess(equipeId);
        return EquipeFuncionariosResponseDTO.builder()
                .idEquipe(equipeId)
            .funcionarios(equipeRepository.findWorkersByEquipeId(equipeId)
                .stream()
                .map(funcionario -> EquipeFuncionarioDTO.builder()
                    .id(funcionario.getId())
                    .nome(funcionario.getNome())
                    .build())
                .toList())
                .build();
    }

        public List<EquipeResponseDTO> findAccessible() {
        Usuario usuario = equipeAccessService.getAuthenticatedUsuarioOrThrow();

        List<Integer> empresaIdsAsAdmin = empresaRepository.findAllByIdAdm(usuario.getId())
            .stream()
            .map(Empresa::getId)
            .toList();

        List<Equipe> equipes = empresaIdsAsAdmin.isEmpty()
            ? equipeRepository.findDistinctByUsuariosId(usuario.getId())
            : equipeRepository.findByEmpresaIdIn(empresaIdsAsAdmin);

        return equipes.stream()
            .map(this::toResponseDTO)
            .toList();
        }

    public void delete(Integer id) {
        if (!equipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipe not found with id: " + id);
        }
        equipeRepository.deleteById(id);
    }

    private EquipeResponseDTO toResponseDTO(Equipe equipe) {
        return EquipeResponseDTO.builder()
                .id(equipe.getId())
                .nomeEmpresa(equipe.getNomeEmpresa())
                .idAdm(equipe.getIdAdm())
                .idUser(equipe.getIdUser())
                .idEmpresa(equipe.getEmpresa() != null ? equipe.getEmpresa().getId() : null)
                .build();
    }

    public List<EquipeResponseDTO> findAllByEmpresa() {
        return equipeRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }
}