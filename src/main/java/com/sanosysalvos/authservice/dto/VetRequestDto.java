package com.sanosysalvos.authservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class VetRequestDto {

    @NotBlank(message = "El nombre de la clínica es requerido")
    @Size(max = 200)
    private String clinicName;

    @Size(max = 300)
    private String address;

    @Size(max = 30)
    private String phone;

    @Size(max = 12)
    private String rutClinica;

    public String getClinicName() { return clinicName; }
    public void setClinicName(String clinicName) { this.clinicName = clinicName; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRutClinica() { return rutClinica; }
    public void setRutClinica(String rutClinica) { this.rutClinica = rutClinica; }
}
