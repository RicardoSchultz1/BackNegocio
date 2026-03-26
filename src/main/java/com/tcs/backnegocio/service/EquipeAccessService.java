package com.tcs.backnegocio.service;

import com.tcs.backnegocio.entity.Usuario;
import com.tcs.backnegocio.exception.UnauthorizedException;
import com.tcs.backnegocio.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EquipeAccessService {

    private final UsuarioRepository usuarioRepository;

    public Integer getEquipeIdOrThrow(Integer usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UnauthorizedException("Usuario not found with id: " + usuarioId));

        if (usuario.getEquipe() == null || usuario.getEquipe().getId() == null) {
            throw new UnauthorizedException("Usuario has no equipe associated");
        }

        return usuario.getEquipe().getId();
    }

    public void validateAccess(Integer usuarioId, Integer equipeId) {
        Integer userEquipeId = getEquipeIdOrThrow(usuarioId);
        if (!userEquipeId.equals(equipeId)) {
            throw new UnauthorizedException("Usuario does not belong to this equipe");
        }
    }
}
