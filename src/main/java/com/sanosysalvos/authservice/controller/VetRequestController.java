package com.sanosysalvos.authservice.controller;

import com.sanosysalvos.authservice.dto.VetRequestDto;
import com.sanosysalvos.authservice.dto.VetRequestResponse;
import com.sanosysalvos.authservice.security.CustomUserDetails;
import com.sanosysalvos.authservice.service.VetRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth/vet-requests")
@Tag(name = "VetRequests", description = "Solicitudes de rol veterinaria")
public class VetRequestController {

    private final VetRequestService vetRequestService;

    public VetRequestController(VetRequestService vetRequestService) {
        this.vetRequestService = vetRequestService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Enviar solicitud de rol veterinaria")
    public ResponseEntity<VetRequestResponse> create(
            @Valid @RequestBody VetRequestDto dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUserId());
        return ResponseEntity.ok(vetRequestService.create(userId, dto));
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Ver estado de mi solicitud")
    public ResponseEntity<VetRequestResponse> getMy(@AuthenticationPrincipal CustomUserDetails userDetails) {
        UUID userId = UUID.fromString(userDetails.getUserId());
        VetRequestResponse response = vetRequestService.getByUserId(userId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Listar todas las solicitudes (solo ADMIN)")
    public ResponseEntity<List<VetRequestResponse>> getAll() {
        return ResponseEntity.ok(vetRequestService.getAll());
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Aprobar solicitud (solo ADMIN)")
    public ResponseEntity<VetRequestResponse> approve(@PathVariable UUID id) {
        return ResponseEntity.ok(vetRequestService.approve(id));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Rechazar solicitud (solo ADMIN)")
    public ResponseEntity<VetRequestResponse> reject(
            @PathVariable UUID id,
            @RequestBody(required = false) Map<String, String> body) {
        String notes = body != null ? body.get("notes") : null;
        return ResponseEntity.ok(vetRequestService.reject(id, notes));
    }
}
