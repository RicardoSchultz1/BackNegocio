package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.usuario.UsuarioCreateDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioResponseDTO;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponseDTO create(UsuarioCreateDTO dto) {
        Equipe equipe = equipeRepository.findById(dto.getIdEquipe())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + dto.getIdEquipe()));

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .dataCadastro(LocalDate.now())
                .equipe(equipe)
                .admSistema(dto.getAdmSistema())
                .build();

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO findById(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario not found with id: " + id));

        return toResponseDTO(usuario);
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    public void delete(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuario not found with id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataCadastro(usuario.getDataCadastro())
                .idEquipe(usuario.getEquipe() != null ? usuario.getEquipe().getId() : null)
                .admSistema(usuario.getAdmSistema())
                .build();
    }
}