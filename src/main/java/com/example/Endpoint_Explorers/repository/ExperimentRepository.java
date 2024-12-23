package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {
    List<Experiment> findByStatus(StatusEnum status);

    @Query("SELECT e FROM Experiment e " +
            "WHERE LOWER(e.algorithm) = LOWER(:algorithm) " +
            "AND LOWER(e.problemName) = LOWER(:problemName) " +
            "AND e.status = :status " +
            "AND e.datetime BETWEEN :startDate AND :endDate")
    List<Experiment> findByAlgorithmAndProblemNameAndStatusAndDatetimeBetweenIgnoreCase(
            @Param("algorithm") String algorithm,
            @Param("problemName") String problemName,
            @Param("status") StatusEnum status,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);
}
