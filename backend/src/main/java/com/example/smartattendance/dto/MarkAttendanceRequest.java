package com.example.smartattendance.dto;

import jakarta.validation.constraints.*;

public class MarkAttendanceRequest {
    @NotBlank
    private String qrToken;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;

    public String getQrToken() { return qrToken; }
    public void setQrToken(String qrToken) { this.qrToken = qrToken; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
