package com.sanosysalvos.authservice.dto;

import com.sanosysalvos.authservice.entity.VetRequestStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class VetRequestResponse {

    private UUID id;
    private UUID userId;
    private String clinicName;
    private String address;
    private String phone;
    private String rutClinica;
    private Double latitude;
    private Double longitude;
    private VetRequestStatus status;
    private String notes;
    private LocalDateTime createdAt;

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
