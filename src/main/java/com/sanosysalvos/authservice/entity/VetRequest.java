package com.sanosysalvos.authservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vet_requests")
public class VetRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "clinic_name", nullable = false, length = 200)
    private String clinicName;

    @Column(length = 300)
    private String address;

    @Column(length = 30)
    private String phone;

    @Column(name = "rut_clinica", length = 12)
    private String rutClinica;

    private Double latitude;

    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VetRequestStatus status = VetRequestStatus.PENDING;

    @Column(length = 500)
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRutClinica() { return rutClinica; }
    public void setRutClinica(String rutClinica) { this.rutClinica = rutClinica; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public VetRequestStatus getStatus() { return status; }
    public void setStatus(VetRequestStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
