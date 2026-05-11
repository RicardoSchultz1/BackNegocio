package com.tcs.backnegocio.controller;

import com.tcs.backnegocio.dto.ia.IaSearchRequestDTO;
import com.tcs.backnegocio.dto.ia.IaSearchResponseDTO;
import com.tcs.backnegocio.service.IaSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ia")
@RequiredArgsConstructor
public class IaSearchController {

    private final IaSearchService iaSearchService;

    @PostMapping("/search")
    public ResponseEntity<IaSearchResponseDTO> search(@RequestBody IaSearchRequestDTO request) {
        return ResponseEntity.ok(iaSearchService.search(request));
    }
}
