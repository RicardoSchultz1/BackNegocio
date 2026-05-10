package com.tcs.backnegocio.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tcs.backnegocio.config.WorkerRedisProperties;
import com.tcs.backnegocio.exception.JobSerializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisDocumentJobPublisherTest {

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ListOperations<String, String> listOperations;

    private final WorkerRedisProperties workerRedisProperties = new WorkerRedisProperties("document_jobs");

    private RedisDocumentJobPublisher redisDocumentJobPublisher;

    @BeforeEach
    void setUp() {
        redisDocumentJobPublisher = new RedisDocumentJobPublisher(stringRedisTemplate, objectMapper, workerRedisProperties);
    }

    @Test
    void shouldEnqueueDocumentWithExpectedPayload() throws Exception {
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"document_id\":123,\"file_path\":\"files/contrato.pdf\"}");

        redisDocumentJobPublisher.enqueueDocument(123, "files/contrato.pdf");

        verify(listOperations).leftPush(eq("document_jobs"), eq("{\"document_id\":123,\"file_path\":\"files/contrato.pdf\"}"));
    }

    @Test
    void shouldThrowJobSerializationExceptionWhenJsonSerializationFails() throws Exception {
        when(objectMapper.writeValueAsString(any())).thenThrow(new JsonProcessingException("boom") { });

        assertThatThrownBy(() -> redisDocumentJobPublisher.enqueueDocument(1, "files/a.pdf"))
                .isInstanceOf(JobSerializationException.class)
                .hasMessage("Failed to serialize document job payload");
    }

    @Test
    void shouldThrowRedisQueueUnavailableWhenRedisFails() throws Exception {
        when(stringRedisTemplate.opsForList()).thenReturn(listOperations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"document_id\":1,\"file_path\":\"files/a.pdf\"}");
        when(listOperations.leftPush(any(), any())).thenThrow(new DataAccessResourceFailureException("redis down"));

        assertThatThrownBy(() -> redisDocumentJobPublisher.enqueueDocument(1, "files/a.pdf"))
                .isInstanceOf(com.tcs.backnegocio.exception.RedisQueueUnavailableException.class)
                .hasMessage("Redis queue is unavailable for document processing");
    }
}
