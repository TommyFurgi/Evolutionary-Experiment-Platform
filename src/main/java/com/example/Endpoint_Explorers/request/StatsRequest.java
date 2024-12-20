package com.example.Endpoint_Explorers.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class StatsRequest {
    private Timestamp startDate;
    private Timestamp endDate;

    public StatsRequest(Timestamp startDate, Timestamp endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
}