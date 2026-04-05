package com.tcs.backnegocio.service;

import com.tcs.backnegocio.dto.usuario.UsuarioCreateDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioEquipeAssignDTO;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final EquipeRepository equipeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioResponseDTO create(UsuarioCreateDTO dto) {
        Set<Equipe> equipes = getEquipesOrThrow(dto.getIdEquipe(), dto.getIdsEquipes());

        Usuario usuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .dataCadastro(LocalDate.now())
                .equipes(equipes)
                .admSistema(dto.getAdmSistema())
                .build();

        return toResponseDTO(usuarioRepository.save(usuario));
    }

    public UsuarioResponseDTO createWorker(UsuarioCreateDTO dto, String admEmail) {
        Usuario adm = usuarioRepository.findWithEquipesByEmail(admEmail)
                .orElseThrow(() -> new ResourceNotFoundException("ADM not found"));

        if (adm.getEquipes() == null || adm.getEquipes().isEmpty()) {
            throw new BusinessException("ADM nao possui empresa associada");
        }

        Integer idEmpresaAdm = adm.getEquipes().stream()
                .map(Equipe::getEmpresa)
                .filter(empresa -> empresa != null)
                .map(empresa -> empresa.getId())
                .findFirst()
                .orElseThrow(() -> new BusinessException("ADM nao possui empresa associada"));

        Set<Equipe> equipesDestino = getEquipesOrThrow(dto.getIdEquipe(), dto.getIdsEquipes());
        boolean hasEquipeForaEmpresa = equipesDestino.stream()
            .anyMatch(equipe -> equipe.getEmpresa() == null || !equipe.getEmpresa().getId().equals(idEmpresaAdm));
        if (hasEquipeForaEmpresa) {
            throw new BusinessException("Uma ou mais equipes informadas nao pertencem a empresa do ADM");
        }

        Usuario novoUsuario = Usuario.builder()
                .nome(dto.getNome())
                .email(dto.getEmail())
                .senha(passwordEncoder.encode(dto.getSenha()))
                .dataCadastro(LocalDate.now())
                .equipes(equipesDestino)
                .admSistema(false)
                .build();

        return toResponseDTO(usuarioRepository.save(novoUsuario));
    }

    public UsuarioLoginResponseDTO login(UsuarioLoginRequestDTO dto) {
        Usuario usuario = usuarioRepository.findWithEquipesByEmail(dto.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Email ou senha invalidos"));

        if (!passwordEncoder.matches(dto.getSenha(), usuario.getSenha())) {
            throw new UnauthorizedException("Email ou senha invalidos");
        }

        String token = jwtUtil.generateToken(usuario.getEmail());

        return UsuarioLoginResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .idEquipe(getPrimaryEquipeId(usuario))
                .idsEquipes(usuario.getEquipes().stream().map(Equipe::getId).sorted().toList())
                .admSistema(usuario.getAdmSistema())
                .token(token)
                .build();
    }

    public UsuarioResponseDTO findById(Integer id) {
        Usuario usuario = usuarioRepository.findWithEquipesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario not found with id: " + id));

        return toResponseDTO(usuario);
    }

    public List<UsuarioResponseDTO> findAll() {
        return usuarioRepository.findAllWithEquipes()
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

    public UsuarioResponseDTO addUserToEquipe(UsuarioEquipeAssignDTO dto, String admEmail) {
        Usuario adm = usuarioRepository.findWithEquipesByEmail(admEmail)
                .orElseThrow(() -> new ResourceNotFoundException("ADM not found"));

        Integer idEmpresaAdm = adm.getEquipes().stream()
                .map(Equipe::getEmpresa)
                .filter(empresa -> empresa != null)
                .map(empresa -> empresa.getId())
                .findFirst()
                .orElseThrow(() -> new BusinessException("ADM nao possui empresa associada"));

        Equipe equipe = equipeRepository.findById(dto.getEquipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + dto.getEquipeId()));

        if (equipe.getEmpresa() == null || !equipe.getEmpresa().getId().equals(idEmpresaAdm)) {
            throw new BusinessException("A equipe informada nao pertence a empresa do ADM");
        }

        Usuario usuario = usuarioRepository.findWithEquipesById(dto.getUsuarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario not found with id: " + dto.getUsuarioId()));

        if (usuario.getEquipes().stream().anyMatch(eq -> eq.getId().equals(equipe.getId()))) {
            throw new BusinessException("Usuario ja pertence a equipe informada");
        }

        usuario.getEquipes().add(equipe);
        Usuario saved = usuarioRepository.save(usuario);
        return toResponseDTO(saved);
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .nome(usuario.getNome())
                .email(usuario.getEmail())
                .dataCadastro(usuario.getDataCadastro())
                .idEquipe(getPrimaryEquipeId(usuario))
                .idsEquipes(usuario.getEquipes().stream().map(Equipe::getId).sorted().toList())
                .admSistema(usuario.getAdmSistema())
                .build();
    }

    private Set<Equipe> getEquipesOrThrow(Integer idEquipe, List<Integer> idsEquipes) {
        Set<Integer> equipeIds = new HashSet<>();
        if (idEquipe != null) {
            equipeIds.add(idEquipe);
        }
        if (idsEquipes != null) {
            equipeIds.addAll(idsEquipes);
        }

        if (equipeIds.isEmpty()) {
            throw new BusinessException("Pelo menos uma equipe deve ser informada");
        }

        Set<Equipe> equipes = new HashSet<>();
        for (Integer equipeId : equipeIds) {
            Equipe equipe = equipeRepository.findById(equipeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Equipe not found with id: " + equipeId));
            equipes.add(equipe);
        }
        return equipes;
    }

    private Integer getPrimaryEquipeId(Usuario usuario) {
        if (usuario.getEquipes() == null || usuario.getEquipes().isEmpty()) {
            return null;
        }
        return usuario.getEquipes().stream()
                .map(Equipe::getId)
                .sorted()
                .findFirst()
                .orElse(null);
    }
}