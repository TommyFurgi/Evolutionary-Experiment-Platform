package com.example.Endpoint_Explorers.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "Experiment")
public class Experiment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String problemName;

    @Column(nullable = false)
    private String algorithm;

    @Column(nullable = false)
    private int numberOfEvaluation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusType status;

    @OneToMany(mappedBy = "experiment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Metrics> metricsList;
}
