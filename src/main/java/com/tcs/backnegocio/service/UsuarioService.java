package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.usuario.UsuarioCreateDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioLoginRequestDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioLoginResponseDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioResponseDTO;
import com.tcs.backnegocio.entity.Equipe;
import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.exception.ResourceNotFoundException;
import com.tcs.backnegocio.exception.UnauthorizedException;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.UsuarioRepository;
import com.tcs.backnegocio.security.JwtUtil;
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
    private final JwtUtil jwtUtil;

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

    public UsuarioResponseDTO createWorker(UsuarioCreateDTO dto, String admEmail) {
        Usuario adm = usuarioRepository.findByEmail(admEmail)
                .orElseThrow(() -> new ResourceNotFoundException("ADM not found"));

        Equipe admEquipe = adm.getEquipe();
        if (admEquipe == null || admEquipe.getEmpresa() == null) {
            throw new BusinessException("ADM nao possui empresa associada");
        }

        Integer idEmpresaAdm = admEquipe.getEmpresa().getId();

        Equipe equipeDestino = equipeRepository.findById(dto.getIdEquipe())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + dto.getIdEquipe()));

        if (equipeDestino.getEmpresa() == null || !equipeDestino.getEmpresa().getId().equals(idEmpresaAdm)) {
            throw new BusinessException("A equipe informada nao pertence a empresa do ADM");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .dataCadastro(LocalDate.now())
                .equipe(equipeDestino)
                .admSistema(false)
                .build();

        return toResponseDTO(usuarioRepository.save(novoUsuario));
    }

    public UsuarioLoginResponseDTO login(UsuarioLoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha invalidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new UnauthorizedException("Email ou senha invalidos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());

        return UsuarioLoginResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .idEquipe(usuario.getEquipe() != null ? usuario.getEquipe().getId() : null)
                .admSistema(usuario.getAdmSistema())
                .token(token)
                .build();
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