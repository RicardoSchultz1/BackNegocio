package com.tcs.backnegocio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "worker.redis")
public record WorkerRedisProperties(String queueName) {

    public String queueNameOrDefault() {
        if (queueName == null || queueName.isBlank()) {
            return "document_jobs";
        }
        return queueName;
    }
}
