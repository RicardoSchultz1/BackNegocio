package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.equipe.EquipeCreateDTO;
import com.tcs.backnegocio.dto.equipe.EquipeResponseDTO;
import com.tcs.backnegocio.service.EquipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/equipes")
@RequiredArgsConstructor
public class EquipeController {

    private final EquipeService equipeService;

    @PostMapping("/create")
    public ResponseEntity<EquipeResponseDTO> create(@Valid @RequestBody EquipeCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(equipeService.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EquipeResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(equipeService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EquipeResponseDTO>> findAll() {
        return ResponseEntity.ok(equipeService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        equipeService.delete(id);
        return ResponseEntity.noContent().build();
    }

}