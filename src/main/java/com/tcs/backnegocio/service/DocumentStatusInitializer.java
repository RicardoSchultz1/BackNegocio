package com.tcs.backnegocio.service;

import com.tcs.backnegocio.entity.DocumentStatus;
import com.tcs.backnegocio.repository.DocumentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentStatusInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_STATUSES = List.of("UPLOADED", "PROCESSING", "PROCESSED", "FAILED");

    private final DocumentStatusRepository documentStatusRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (String statusName : DEFAULT_STATUSES) {
            documentStatusRepository.findByStatusName(statusName)
                    .orElseGet(() -> documentStatusRepository.save(DocumentStatus.builder().statusName(statusName).build()));
        }
    }
}
