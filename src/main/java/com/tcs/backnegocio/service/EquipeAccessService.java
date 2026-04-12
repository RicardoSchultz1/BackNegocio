package com.tcs.backnegocio.service;

import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.UnauthorizedException;
import com.tcs.backnegocio.repository.EmpresaRepository;
import com.tcs.backnegocio.repository.EquipeRepository;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EquipeAccessService {

    private final UsuarioRepository usuarioRepository;
    private final EmpresaRepository empresaRepository;
    private final EquipeRepository equipeRepository;

    public Integer getEquipeIdOrThrow(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findWithEquipesById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Usuario not found with id: " + usuarioId));

        if (usuario.getEquipes() == null || usuario.getEquipes().isEmpty()) {
            throw new UnauthorizedException("Usuario has no equipe associated");
        }

        return usuario.getEquipes().stream()
                .map(equipe -> equipe.getId())
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Usuario has no equipe associated"));
    }

    public Usuario getAuthenticatedUsuarioOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new UnauthorizedException("Authenticated user not found");
        }

        return usuarioRepository.findWithEquipesByEmail(authentication.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public Integer getAuthenticatedEquipeIdOrThrow() {
        List<Integer> accessibleEquipeIds = getAccessibleEquipeIdsOrThrow(getAuthenticatedUsuarioOrThrow());
        if (accessibleEquipeIds.isEmpty()) {
            throw new UnauthorizedException("Usuario has no equipe associated");
        }
        return accessibleEquipeIds.stream()
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Usuario has no equipe associated"));
    }

    public void validateCurrentUserAccess(Integer equipeId) {
        Usuario usuario = getAuthenticatedUsuarioOrThrow();
        boolean hasAccess = hasDirectEquipeAccess(usuario, equipeId) || hasCompanyAdminAccess(usuario, equipeId);
        if (!hasAccess) {
            throw new UnauthorizedException("Usuario does not belong to this equipe");
        }
    }

    public void validateAccess(Integer usuarioId, Integer equipeId) {
        Usuario usuario = usuarioRepository.findWithEquipesById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Usuario not found with id: " + usuarioId));
        boolean hasAccess = hasDirectEquipeAccess(usuario, equipeId) || hasCompanyAdminAccess(usuario, equipeId);
        if (!hasAccess) {
            throw new UnauthorizedException("Usuario does not belong to this equipe");
        }
    }

    public List<Integer> getAccessibleEquipeIdsForCurrentUser() {
        return getAccessibleEquipeIdsOrThrow(getAuthenticatedUsuarioOrThrow());
    }

    private List<Integer> getAccessibleEquipeIdsOrThrow(Usuario usuario) {
        Set<Integer> equipeIds = new LinkedHashSet<>();

        if (usuario.getEquipes() != null) {
            usuario.getEquipes().stream()
                    .map(equipe -> equipe.getId())
                    .forEach(equipeIds::add);
        }

        List<Integer> empresaIdsAsAdmin = empresaRepository.findAllByIdAdm(usuario.getId())
                .stream()
                .map(empresa -> empresa.getId())
                .toList();

        if (!empresaIdsAsAdmin.isEmpty()) {
            equipeRepository.findByEmpresaIdIn(empresaIdsAsAdmin)
                    .stream()
                    .map(equipe -> equipe.getId())
                    .forEach(equipeIds::add);
        }

        if (equipeIds.isEmpty()) {
            throw new UnauthorizedException("Usuario has no equipe associated");
        }

        return equipeIds.stream().toList();
    }

    private boolean hasDirectEquipeAccess(Usuario usuario, Integer equipeId) {
        return usuario.getEquipes() != null
                && usuario.getEquipes().stream().anyMatch(equipe -> equipe.getId().equals(equipeId));
    }

    private boolean hasCompanyAdminAccess(Usuario usuario, Integer equipeId) {
        List<Integer> empresaIdsAsAdmin = empresaRepository.findAllByIdAdm(usuario.getId())
                .stream()
                .map(empresa -> empresa.getId())
                .toList();

        return !empresaIdsAsAdmin.isEmpty()
                && equipeRepository.findByEmpresaIdIn(empresaIdsAsAdmin)
                .stream()
                .anyMatch(equipe -> equipe.getId().equals(equipeId));
    }
}
