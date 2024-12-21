package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {
    List<Experiment> findByStatus(StatusEnum status);

    List<Experiment> findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(String algorithm, String problemName,  StatusEnum status, Timestamp startDate, Timestamp endDate);
}
