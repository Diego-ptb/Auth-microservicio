package com.sanosysalvos.authservice.service;

import com.sanosysalvos.authservice.dto.VetRequestDto;
import com.sanosysalvos.authservice.dto.VetRequestResponse;
import com.sanosysalvos.authservice.entity.Role;
import com.sanosysalvos.authservice.entity.User;
import com.sanosysalvos.authservice.entity.VetRequest;
import com.sanosysalvos.authservice.entity.VetRequestStatus;
import com.sanosysalvos.authservice.repository.RoleRepository;
import com.sanosysalvos.authservice.repository.UserRepository;
import com.sanosysalvos.authservice.repository.VetRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class VetRequestService {

    private final VetRequestRepository vetRequestRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailService emailService;

    public VetRequestService(VetRequestRepository vetRequestRepository, UserRepository userRepository,
                              RoleRepository roleRepository, EmailService emailService) {
        this.vetRequestRepository = vetRequestRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.emailService = emailService;
    }

    @Transactional
    public VetRequestResponse create(UUID userId, VetRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<VetRequest> existing = vetRequestRepository.findByUserId(userId);
        VetRequest request;

        if (existing.isPresent()) {
            request = existing.get();
            if (request.getStatus() == VetRequestStatus.PENDING) {
                throw new RuntimeException("Ya tienes una solicitud pendiente de revisión");
            }
            request.setClinicName(dto.getClinicName());
            request.setAddress(dto.getAddress());
            request.setPhone(dto.getPhone());
            request.setRutClinica(dto.getRutClinica());
            if (request.getStatus() == VetRequestStatus.REJECTED) {
                request.setStatus(VetRequestStatus.PENDING);
                request.setNotes(null);
            }
            // APPROVED: keep status, only update clinic data
        } else {
            request = new VetRequest();
            request.setUserId(userId);
            request.setClinicName(dto.getClinicName());
            request.setAddress(dto.getAddress());
            request.setPhone(dto.getPhone());
            request.setRutClinica(dto.getRutClinica());
        }

        vetRequestRepository.save(request);
        emailService.sendVetRequestNotification(dto.getClinicName(), user.getEmail(), user.getUsername());
        return toResponse(request);
    }

    public VetRequestResponse getByUserId(UUID userId) {
        return vetRequestRepository.findByUserId(userId)
                .map(this::toResponse)
                .orElse(null);
    }

    public List<VetRequestResponse> getAll() {
        return vetRequestRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VetRequestResponse approve(UUID requestId) {
        VetRequest request = vetRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        request.setStatus(VetRequestStatus.APPROVED);
        vetRequestRepository.save(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Role refugioRole = roleRepository.findByName("REFUGIO")
                .orElseThrow(() -> new RuntimeException("Rol REFUGIO no encontrado"));
        user.setRole(refugioRole);
        userRepository.save(user);

        emailService.sendVetRequestApproved(user.getEmail(), request.getClinicName());
        return toResponse(request);
    }

    @Transactional
    public VetRequestResponse reject(UUID requestId, String notes) {
        VetRequest request = vetRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        request.setStatus(VetRequestStatus.REJECTED);
        request.setNotes(notes);
        vetRequestRepository.save(request);

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        emailService.sendVetRequestRejected(user.getEmail(), request.getClinicName(), notes);
        return toResponse(request);
    }

    private VetRequestResponse toResponse(VetRequest r) {
        VetRequestResponse resp = new VetRequestResponse();
        resp.setId(r.getId());
        resp.setUserId(r.getUserId());
        resp.setClinicName(r.getClinicName());
        resp.setAddress(r.getAddress());
        resp.setPhone(r.getPhone());
        resp.setRutClinica(r.getRutClinica());
        resp.setStatus(r.getStatus());
        resp.setNotes(r.getNotes());
        resp.setCreatedAt(r.getCreatedAt());
        return resp;
    }
}
