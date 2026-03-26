package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.empresa.EmpresaCreateDTO;
import com.tcs.backnegocio.dto.empresa.EmpresaResponseDTO;
import com.tcs.backnegocio.entity.Empresa;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaResponseDTO create(EmpresaCreateDTO dto) {
        Empresa empresa = Empresa.builder()
                .nome(dto.getNome())
                .cnpj(dto.getCnpj())
                .idAdm(dto.getIdAdm())
                .dataCadastro(LocalDate.now())
                .build();

        return toResponseDTO(empresaRepository.save(empresa));
    }

    public EmpresaResponseDTO findById(Integer id) {
        Empresa empresa = empresaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa not found with id: " + id));

        return toResponseDTO(empresa);
    }

    public List<EmpresaResponseDTO> findAll() {
        return empresaRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void delete(Integer id) {
        if (!empresaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Empresa not found with id: " + id);
        }
        empresaRepository.deleteById(id);
    }

    private EmpresaResponseDTO toResponseDTO(Empresa empresa) {
        return EmpresaResponseDTO.builder()
                .id(empresa.getId())
                .nome(empresa.getNome())
                .cnpj(empresa.getCnpj())
                .dataCadastro(empresa.getDataCadastro())
                .idAdm(empresa.getIdAdm())
                .build();
    }
}