package com.endpointexplorers.server.repository;

import com.endpointexplorers.server.model.Metrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MetricsRepository extends JpaRepository<Metrics, Integer> {
    List<Metrics> findByExperimentId(int experimentId);

    @Modifying
    @Query("DELETE FROM Metrics m WHERE m.experiment.id = :experimentId")
    void deleteByExperimentId(@Param("experimentId") int experimentId);

    @Modifying
    @Query("DELETE FROM Metrics m WHERE m.experiment.id IN :experimentIds")
    void deleteByExperimentIds(@Param("experimentIds") List<Integer> experimentIds);

}
