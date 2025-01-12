package com.example.Endpoint_Explorers.repository;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Repository
public interface ExperimentRepository extends JpaRepository<Experiment, Integer> {
    List<Experiment> findByStatus(StatusEnum status);

    @Query("SELECT e FROM Experiment e "
            + "WHERE e.algorithm = :algorithm "
            + "AND e.problemName = :problemName "
            + "AND e.status = :status "
            + "AND e.datetime BETWEEN :startDate AND :endDate "
            + "AND (:groupName IS NULL OR :groupName = '' OR e.groupName = :groupName)")
    List<Experiment> findByAlgorithmAndProblemNameAndStatusAndDatetimeBetween(
            @Param("algorithm") String algorithm,
            @Param("problemName") String problemName,
            @Param("status") StatusEnum status,
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate,
            @Param("groupName") String groupName);

    @Query("SELECT e FROM Experiment e "
            + "LEFT JOIN e.metricsList m "
            + "WHERE "
            + "(:statuses IS NULL OR e.status IN :statuses) AND "
            + "(:problems IS NULL OR e.problemName IN :problems) AND "
            + "(:algorithms IS NULL OR e.algorithm IN :algorithms) AND "
            + "(:metrics IS NULL OR m.metricsName IN :metrics) AND"
            + "(:groups IS NULL OR e.groupName IN :groups)")
    List<Experiment> findFilteredExperiments(
            @Param("statuses") Set<String> statuses,
            @Param("problems") Set<String> problems,
            @Param("algorithms") Set<String> algorithms,
            @Param("metrics") Set<String> metrics,
            @Param("groups") Set<String> groups);
}
