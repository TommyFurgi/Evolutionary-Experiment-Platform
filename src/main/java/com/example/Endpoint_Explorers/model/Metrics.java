package com.example.Endpoint_Explorers.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Metrics")
public class Metrics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String metricsName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore //DTO in the future
    @JoinColumn(nullable = false)
    private Experiment experiment;

    @Column(nullable = false)
    private int iterationNumber;

    @Column(nullable = false)
    private float value;
}
