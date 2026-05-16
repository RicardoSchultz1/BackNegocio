package com.tcs.backnegocio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.backnegocio.config.IaSearchProperties;
import com.tcs.backnegocio.dto.ia.IaAskChunksInputDTO;
import com.tcs.backnegocio.dto.ia.IaAskChunksRequestDTO;
import com.tcs.backnegocio.dto.ia.IaSearchRequestDTO;
import com.tcs.backnegocio.dto.ia.IaSearchDocumentDTO;
import com.tcs.backnegocio.dto.ia.IaSearchResponseDTO;
import com.tcs.backnegocio.exception.BusinessException;
import com.tcs.backnegocio.repository.DocumentChunkRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Objects;

import java.util.Collections;
import java.util.List;

@Service
public class IaSearchService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final IaSearchProperties iaSearchProperties;
    private final DocumentChunkRepository documentChunkRepository;
    private final ObjectMapper objectMapper;

    public IaSearchService(
            IaSearchProperties iaSearchProperties,
            DocumentChunkRepository documentChunkRepository,
            ObjectMapper objectMapper
    ) {
        this.iaSearchProperties = iaSearchProperties;
        this.documentChunkRepository = documentChunkRepository;
        this.objectMapper = objectMapper;
    }

    public IaSearchResponseDTO search(IaSearchRequestDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<IaSearchRequestDTO> entity = new HttpEntity<>(request, headers);

            var response = restTemplate.postForEntity(
                    iaSearchProperties.searchUrlOrDefault(),
                    entity,
                    IaSearchResponseDTO.class
            );

            if (response.getBody() == null) {
                throw new BusinessException(
                        "IA search returned empty response",
                        HttpStatus.BAD_GATEWAY
                );
            }

            return response.getBody();

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(
                    "Failed to search IA: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    public IaSearchResponseDTO askChunks(IaAskChunksInputDTO request) {
        try {

            if (request.getQuestion() == null || request.getQuestion().isBlank()) {
                throw new BusinessException(
                        "Question is required",
                        HttpStatus.BAD_REQUEST
                );
            }

            if (request.getDocumentId() == null) {
                throw new BusinessException(
                        "Document id is required",
                        HttpStatus.BAD_REQUEST
                );
            }

            List<Integer> chunkIndexes = documentChunkRepository
                    .findByDocumentIdOrderByChunkIndexAsc(request.getDocumentId())
                    .stream()
                    .map(chunk -> chunk.getChunkIndex())
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();

            if (chunkIndexes.isEmpty()) {
                throw new BusinessException(
                        "No chunks found for document id " + request.getDocumentId(),
                        HttpStatus.NOT_FOUND
                );
            }

            IaAskChunksRequestDTO iaRequest = new IaAskChunksRequestDTO(
                    request.getQuestion(),
                    request.getDocumentId(),
                    chunkIndexes
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<IaAskChunksRequestDTO> entity =
                    new HttpEntity<>(iaRequest, headers);

            var response = restTemplate.postForEntity(
                    iaSearchProperties.askChunksUrlOrDefault(),
                    entity,
                    String.class
            );

            return parseAskChunksResponse(response.getBody());

        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(
                    "Failed to ask IA with chunks: " + ex.getMessage(),
                    HttpStatus.BAD_GATEWAY
            );
        }
    }

    private IaSearchResponseDTO parseAskChunksResponse(String responseBody)
            throws JsonProcessingException {

        if (responseBody == null || responseBody.isBlank()) {
            throw new BusinessException(
                    "IA search returned empty response",
                    HttpStatus.BAD_GATEWAY
            );
        }

        JsonNode root = objectMapper.readTree(responseBody);

        List<IaSearchDocumentDTO> documents;

        if (root.isArray()) {

            documents = objectMapper.convertValue(
                    root,
                    new TypeReference<>() {}
            );

        } else if (root.has("documents")
                && root.get("documents").isArray()) {

            documents = objectMapper.convertValue(
                    root.get("documents"),
                    new TypeReference<>() {}
            );

        } else if (root.has("data")
                && root.get("data").has("documents")
                && root.get("data").get("documents").isArray()) {

            documents = objectMapper.convertValue(
                    root.get("data").get("documents"),
                    new TypeReference<>() {}
            );

        } else {

            String preview = responseBody.length() > 300
                    ? responseBody.substring(0, 300) + "..."
                    : responseBody;

            throw new BusinessException(
                    "Unexpected IA response format for /ask/chunks: " + preview,
                    HttpStatus.BAD_GATEWAY
            );
        }

        IaSearchResponseDTO normalizedResponse =
                new IaSearchResponseDTO();

        normalizedResponse.setDocuments(
                documents == null
                        ? Collections.emptyList()
                        : documents
        );

        return normalizedResponse;
    }
}
