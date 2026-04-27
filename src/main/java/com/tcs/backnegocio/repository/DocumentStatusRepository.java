package com.tcs.backnegocio.repository;

import com.tcs.backnegocio.entity.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentStatusRepository extends JpaRepository<DocumentStatus, Integer> {

    Optional<DocumentStatus> findByStatusName(String statusName);
}
