package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Integer> {
    List<Metrics> findByExperimentId(int experimentId);
}
