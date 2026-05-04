package com.tcs.backnegocio.service;

import com.tcs.backnegocio.entity.DocumentStatus;
import com.tcs.backnegocio.repository.DocumentStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class DocumentStatusInitializer implements ApplicationRunner {

    private static final Map<Integer, String> DEFAULT_STATUSES = new LinkedHashMap<>();

    static {
        DEFAULT_STATUSES.put(1, "UPLOADED");
        DEFAULT_STATUSES.put(2, "PROCESSING");
        DEFAULT_STATUSES.put(3, "PROCESSED");
        DEFAULT_STATUSES.put(4, "FAILED");
    }

    private final DocumentStatusRepository documentStatusRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (Map.Entry<Integer, String> status : DEFAULT_STATUSES.entrySet()) {
            Integer id = status.getKey();
            String statusName = status.getValue();

            if (documentStatusRepository.findById(id).isPresent()) {
                continue;
            }

            documentStatusRepository.findByStatusName(statusName)
                    .orElseGet(() -> documentStatusRepository.save(
                            DocumentStatus.builder()
                                    .id(id)
                                    .statusName(statusName)
                                    .build()
                    ));
        }
    }
}
