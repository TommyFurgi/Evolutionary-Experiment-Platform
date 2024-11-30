package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Experiment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {
}
