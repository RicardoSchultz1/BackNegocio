package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.empresa.EmpresaCreateDTO;
import com.tcs.backnegocio.dto.empresa.EmpresaCreateRequestDTO;
import com.tcs.backnegocio.dto.empresa.EmpresaResponseDTO;
import com.tcs.backnegocio.entity.Empresa;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.repository.EmpresaRepository;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EmpresaOnboardingService {

    private final EmpresaRepository empresaRepository;
    private final EquipeRepository equipeRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public EmpresaResponseDTO createEmpresaWithAdminAndEquipe(EmpresaCreateRequestDTO requestDTO) {
        EmpresaCreateDTO dtoEmpresa = requestDTO.getEmpresa();

        Empresa empresa = Empresa.builder()
                .nome(dtoEmpresa.getNome())
                .cnpj(dtoEmpresa.getCnpj())
                .dataCadastro(LocalDate.now())
                .build();
        empresa = empresaRepository.save(empresa);

        Equipe equipe = Equipe.builder()
                .nomeEmpresa(requestDTO.getEquipe().getNomeEmpresa())
                .empresa(empresa)
                .build();
        equipe = equipeRepository.save(equipe);

        Usuario usuario = Usuario.builder()
                .nome(requestDTO.getUsuario().getNome())
                .email(requestDTO.getUsuario().getEmail())
                .senha(passwordEncoder.encode(requestDTO.getUsuario().getSenha()))
                .dataCadastro(LocalDate.now())
                .equipes(new HashSet<>(Set.of(equipe)))
                .admSistema(Boolean.TRUE)
                .build();
        usuario = usuarioRepository.save(usuario);

        empresa.setIdAdm(usuario.getId());
        empresa = empresaRepository.save(empresa);

        equipe.setIdAdm(usuario.getId());
        equipe.setIdUser(usuario.getId());
        equipeRepository.save(equipe);

        return EmpresaResponseDTO.builder()
                .id(empresa.getId())
                .nome(empresa.getNome())
                .cnpj(empresa.getCnpj())
                .dataCadastro(empresa.getDataCadastro())
                .idAdm(empresa.getIdAdm())
                .build();
    }
}