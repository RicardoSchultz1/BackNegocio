package com.tcs.backnegocio.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.backnegocio.config.WorkerRedisProperties;
import com.tcs.backnegocio.exception.JobSerializationException;
import com.tcs.backnegocio.exception.RedisQueueUnavailableException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisDocumentJobPublisher implements DocumentJobPublisher {

    private static final Logger log = LoggerFactory.getLogger(RedisDocumentJobPublisher.class);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final WorkerRedisProperties workerRedisProperties;

    @Override
    public void enqueueDocument(Integer documentId, String filePath) {
        String queueName = workerRedisProperties.queueNameOrDefault();
        String payload = serializePayload(documentId, filePath, queueName);

        try {
            stringRedisTemplate.opsForList().leftPush(queueName, payload);
        } catch (DataAccessException ex) {
            throw new RedisQueueUnavailableException(
                    "Redis queue is unavailable for document processing", ex);
        }

        log.info("event=redis_job_published documentId={} filePath={} queueName={}", documentId, filePath, queueName);
    }

    private String serializePayload(Integer documentId, String filePath, String queueName) {
        try {
            return objectMapper.writeValueAsString(new DocumentJobPayload(documentId, filePath));
        } catch (JsonProcessingException ex) {
            log.error("event=redis_job_serialize_error documentId={} filePath={} queueName={} message={}",
                    documentId, filePath, queueName, ex.getMessage());
            throw new JobSerializationException("Failed to serialize document job payload", ex);
        }
    }

    private record DocumentJobPayload(
            @JsonProperty("document_id") Integer documentId,
            @JsonProperty("file_path") String filePath
    ) {
    }
}
