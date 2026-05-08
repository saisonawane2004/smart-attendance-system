package com.example.smartattendance.dto;

import jakarta.validation.constraints.NotNull;

public class SessionCreateRequest {
    @NotNull
    private Long subjectId;
    @NotNull
    private Double latitude;
    @NotNull
    private Double longitude;
    @NotNull
    private Double radius;

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public Double getRadius() { return radius; }
    public void setRadius(Double radius) { this.radius = radius; }
}
