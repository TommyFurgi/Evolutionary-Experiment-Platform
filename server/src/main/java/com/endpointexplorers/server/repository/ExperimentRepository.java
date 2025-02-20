package com.endpointexplorers.server.repository;

import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.StatusEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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
            + "AND (:groupName IS NULL OR :groupName = '' OR e.groupName = :groupName)"
            + "ORDER BY e.id")
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
            + "(:groups IS NULL OR e.groupName IN :groups)"
            + "ORDER BY e.id")
    List<Experiment> findFilteredExperiments(
            @Param("statuses") Set<String> statuses,
            @Param("problems") Set<String> problems,
            @Param("algorithms") Set<String> algorithms,
            @Param("metrics") Set<String> metrics,
            @Param("groups") Set<String> groups);

    List<Experiment> findByGroupName(String groupName);

    @Modifying
    @Query("UPDATE Experiment e SET e.groupName = :newGroupName WHERE e.id IN :experimentIds")
    void updateGroupForExperiments(@Param("experimentIds") List<Integer> experimentIds,
                                   @Param("newGroupName") String newGroupName);

    @Modifying
    @Query("DELETE FROM Experiment e WHERE e.id = :id")
    int deleteById(@Param("id") int id);

    @Modifying
    @Query("DELETE FROM Experiment e WHERE e.groupName = :groupName")
    int deleteByGroupName(@Param("groupName") String groupName);
}
