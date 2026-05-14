package com.tcs.backnegocio.service;

import com.tcs.backnegocio.config.IaSearchProperties;
import com.tcs.backnegocio.dto.ia.IaSearchRequestDTO;
import com.tcs.backnegocio.dto.ia.IaSearchResponseDTO;
import com.tcs.backnegocio.exception.BusinessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class IaSearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final IaSearchProperties iaSearchProperties;

    public IaSearchService(IaSearchProperties iaSearchProperties) {
        this.iaSearchProperties = iaSearchProperties;
    }

    public IaSearchResponseDTO search(IaSearchRequestDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<IaSearchRequestDTO> entity = new HttpEntity<>(request, headers);
            
            var response = restTemplate.postForEntity(
                    iaSearchProperties.urlOrDefault(),
                    entity,
                    IaSearchResponseDTO.class
            );
            
            if (response.getBody() == null) {
                throw new BusinessException("IA search returned empty response", HttpStatus.BAD_GATEWAY);
            }
            
            return response.getBody();
        } catch (Exception ex) {
            throw new BusinessException("Failed to search IA: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }
}
