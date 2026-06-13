package com.sanosysalvos.authservice.repository;

import com.sanosysalvos.authservice.entity.VetRequest;
import com.sanosysalvos.authservice.entity.VetRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VetRequestRepository extends JpaRepository<VetRequest, UUID> {
    Optional<VetRequest> findByUserId(UUID userId);
    List<VetRequest> findByStatus(VetRequestStatus status);
}
