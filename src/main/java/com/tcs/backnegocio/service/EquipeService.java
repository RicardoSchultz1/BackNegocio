package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.equipe.EquipeCreateDTO;
import com.tcs.backnegocio.dto.equipe.EquipeResponseDTO;
import com.tcs.backnegocio.entity.Empresa;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.EmpresaRepository;
import com.tcs.backnegocio.repository.EquipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EquipeService {

    private final EquipeRepository equipeRepository;
    private final EmpresaRepository empresaRepository;
    private final FolderService folderService;

    public EquipeResponseDTO create(EquipeCreateDTO dto) {
        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                .orElseThrow(() -> new ResourceNotFoundException("Empresa not found with id: " + dto.getIdEmpresa()));

        Equipe equipe = Equipe.builder()
                .nomeEmpresa(dto.getNomeEmpresa())
                .idAdm(dto.getIdAdm())
                .idUser(dto.getIdUser())
                .empresa(empresa)
                .build();

        Equipe saved = equipeRepository.save(equipe);
        folderService.ensureRootFolder(saved.getId());
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
}