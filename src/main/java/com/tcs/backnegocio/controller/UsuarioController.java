package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.usuario.UsuarioCreateDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioLoginRequestDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioLoginResponseDTO;
import com.tcs.backnegocio.dto.usuario.UsuarioResponseDTO;
import com.tcs.backnegocio.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping("/create")
    public ResponseEntity<UsuarioResponseDTO> create(@Valid @RequestBody UsuarioCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.create(dto));
    }

    @PostMapping("/login")
    public ResponseEntity<UsuarioLoginResponseDTO> login(@Valid @RequestBody UsuarioLoginRequestDTO dto) {
        return ResponseEntity.ok(usuarioService.login(dto));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<UsuarioResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<UsuarioResponseDTO>> findAll() {
        return ResponseEntity.ok(usuarioService.findAll());
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        usuarioService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADM')")
    @PostMapping("/create-new-worker")
    public ResponseEntity<UsuarioResponseDTO> createNewWorker(@Valid @RequestBody UsuarioCreateDTO dto,
                                                              @AuthenticationPrincipal String admEmail) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.createWorker(dto, admEmail));
    }
}