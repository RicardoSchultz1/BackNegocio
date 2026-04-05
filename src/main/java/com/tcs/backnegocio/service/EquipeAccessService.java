package com.tcs.backnegocio.service;

import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.UnauthorizedException;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipeAccessService {

    private final UsuarioRepository usuarioRepository;

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
        Usuario usuario = getAuthenticatedUsuarioOrThrow();
        if (usuario.getEquipes() == null || usuario.getEquipes().isEmpty()) {
            throw new UnauthorizedException("Usuario has no equipe associated");
        }
        return usuario.getEquipes().stream()
                .map(equipe -> equipe.getId())
                .findFirst()
                .orElseThrow(() -> new UnauthorizedException("Usuario has no equipe associated"));
    }

    public void validateCurrentUserAccess(Integer equipeId) {
        Usuario usuario = getAuthenticatedUsuarioOrThrow();
        boolean hasAccess = usuario.getEquipes().stream().anyMatch(equipe -> equipe.getId().equals(equipeId));
        if (!hasAccess) {
            throw new UnauthorizedException("Usuario does not belong to this equipe");
        }
    }

    public void validateAccess(Integer usuarioId, Integer equipeId) {
        Usuario usuario = usuarioRepository.findWithEquipesById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Usuario not found with id: " + usuarioId));
        boolean hasAccess = usuario.getEquipes().stream().anyMatch(equipe -> equipe.getId().equals(equipeId));
        if (!hasAccess) {
            throw new UnauthorizedException("Usuario does not belong to this equipe");
        }
    }
}
