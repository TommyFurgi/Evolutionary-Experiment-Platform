package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Integer> {
}
