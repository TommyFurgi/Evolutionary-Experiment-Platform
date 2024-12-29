package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.StatusEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExperimentValidatorTest {

    private ExperimentValidator validator;

    @BeforeEach
    void setUp() throws Exception {
        validator = new ExperimentValidator();

        //  "allRegisteredProblems" przez refleksję
        Field problemsField = ExperimentValidator.class
                .getDeclaredField("allRegisteredProblems");
        problemsField.setAccessible(true);
        problemsField.set(validator, Set.of("uf1", "dtlz2", "zdt1"));

        Field algosField = ExperimentValidator.class
                .getDeclaredField("allAlgorithms");
        algosField.setAccessible(true);
        algosField.set(validator, Set.of("nsga-ii", "e-moea", "gde3"));
    }

    @Test
    void testValidateProblemName_ok() {
        assertDoesNotThrow(() -> validator.validateExperimentParams("uf1", "nsga-ii",
                List.of("spacing"), 1000, 1));
    }

    @Test
    void testValidateProblemName_notFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("xxx", "nsga-ii",
                        List.of("hypervolume"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Problem not found: xxx"));
    }

    @Test
    void testValidateAlgorithm_notFound() {
        // "fancy-algo" nie znajduje się w set {"nsga-ii", "e-moea", "gde3"}
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "fancy-algo",
                        List.of("hypervolume"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Algorithm not found: fancy-algo"));
    }

    @Test
    void testValidateMetrics_empty() {
        // brak metryk
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of(), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Metrics list cannot be empty."));
    }

    @Test
    void testValidateMetrics_unknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("unknownMetric"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Unknown metric specified: unknownMetric"));
    }

    @Test
    void testValidateEvaluationNumber_zeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("spacing"), -1, 1)
        );
        assertTrue(ex.getMessage().contains("Evaluation number must be greater than 0"));
    }

    @Test
    void testValidateIterationNumber_negative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("spacing"), 100, -5)
        );
        assertTrue(ex.getMessage().contains("Iteration number must be greater than 0"));
    }

    @Test
    void testValidateDates_startAfterEnd() {
        Timestamp start = Timestamp.valueOf("2024-01-02 00:00:00");
        Timestamp end = Timestamp.valueOf("2024-01-01 00:00:00");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateStatsParams("uf1", "nsga-ii", start, end)
        );
        assertTrue(ex.getMessage().contains("Start date cannot be after end date."));
    }

    @Test
    void testValidateDates_nullDates() {
        // endDate=null
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateStatsParams("uf1", "gde3",
                        Timestamp.valueOf("2024-01-01 00:00:00"), null)
        );
        assertTrue(ex.getMessage().contains("End date cannot be null."));
    }

    @Test
    void testValidateStatus_unknown() {
        // W validateListParams(...) => statuses.forEach(this::validateStatus)
        // i validateStatus => StatusEnum.valueOf(...)
        // "IN_PROGRESS" / "FAILED" / "READY" / "COMPLETED" => ok
        // "whatever" => błąd
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateListParams(List.of("whatever"), List.of("uf1"),
                        List.of("nsga-ii"), List.of("hypervolume"))
        );
        assertTrue(ex.getMessage().contains("Unknown status specified: whatever"));
    }

    @Test
    void testValidateStatus_ok() {
        assertDoesNotThrow(() ->
                validator.validateListParams(List.of("READY", "COMPLETED"),
                        List.of("uf1"), List.of("nsga-ii"), List.of("spacing"))
        );
    }
}
