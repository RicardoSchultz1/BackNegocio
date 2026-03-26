package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.empresa.EmpresaCreateRequestDTO;
import com.tcs.backnegocio.dto.empresa.EmpresaResponseDTO;
import com.tcs.backnegocio.service.EmpresaOnboardingService;
import com.tcs.backnegocio.service.EmpresaService;
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
@RequestMapping("/empresas")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService empresaService;
    private final EmpresaOnboardingService empresaOnboardingService;

    /*
    @PostMapping("/create")
    public ResponseEntity<EmpresaResponseDTO> create(@Valid @RequestBody EmpresaCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empresaService.create(dto));
    }
    */
    @PostMapping("/create")
    public ResponseEntity<EmpresaResponseDTO> create(@Valid @RequestBody EmpresaCreateRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(empresaOnboardingService.createEmpresaWithAdminAndEquipe(requestDTO));
    }


    @GetMapping("/{id}")
    public ResponseEntity<EmpresaResponseDTO> findById(@PathVariable Integer id) {
        return ResponseEntity.ok(empresaService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<List<EmpresaResponseDTO>> findAll() {
        return ResponseEntity.ok(empresaService.findAll());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        empresaService.delete(id);
        return ResponseEntity.noContent().build();
    }
}