package com.tcs.backnegocio.storage;

import com.tcs.backnegocio.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class SupabaseStorageService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api-key}")
    private String apiKey;

    @Value("${supabase.storage.bucket}")
    private String bucket;

    public String upload(MultipartFile file, Integer equipeId, Integer folderId) {
        String sanitizedName = Objects.requireNonNull(file.getOriginalFilename(), "file name is required")
            .replaceAll("[^a-zA-Z0-9._-]", "_");
        String path = "equipe-" + equipeId + "/folder-" + folderId + "/" + UUID.randomUUID() + "-" + sanitizedName;

        String endpoint = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            headers.set("Authorization", "Bearer " + apiKey);
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("x-upsert", "false");

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            ResponseEntity<String> response = restTemplate.exchange(endpoint, HttpMethod.POST, entity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BusinessException("Supabase upload failed with status: " + response.getStatusCode().value());
            }

            return path;
        } catch (IOException ex) {
            throw new BusinessException("Could not read file bytes for upload");
        } catch (Exception ex) {
            throw new BusinessException("Supabase upload failed: " + ex.getMessage());
        }
    }

    public String buildPublicUrl(String path) {
        return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + path;
    }

    public byte[] download(String path) {
        String endpoint = supabaseUrl + "/storage/v1/object/" + bucket + "/" + path;

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("apikey", apiKey);
            headers.set("Authorization", "Bearer " + apiKey);

            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(endpoint, HttpMethod.GET, entity, byte[].class);

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BusinessException("Supabase download failed with status: " + response.getStatusCode().value());
            }

            return response.getBody();
        } catch (Exception ex) {
            throw new BusinessException("Supabase download failed: " + ex.getMessage());
        }
    }
}
