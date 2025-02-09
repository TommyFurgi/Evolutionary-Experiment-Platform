package com.example.Endpoint_Explorers.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.util.ArrayList;
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
    void validateProblemNameOk() {
        assertDoesNotThrow(() -> validator.validateExperimentParams("uf1", "nsga-ii",
                List.of("spacing"), 1000, 1));
    }

    @Test
    void validateProblemNameNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("xxx", "nsga-ii",
                        List.of("hypervolume"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Problem not found: xxx"));
    }

    @Test
    void validateAlgorithmNotFound() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "fancy-algo",
                        List.of("hypervolume"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Algorithm not found: fancy-algo"));
    }

    @Test
    void validateMetricsEmpty() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of(), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Metrics list cannot be empty."));
    }

    @Test
    void validateMetricsUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("unknownMetric"), 1000, 1)
        );
        assertTrue(ex.getMessage().contains("Unknown metric specified: unknownMetric"));
    }

    @Test
    void validateEvaluationNumberZeroOrNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("spacing"), -1, 1)
        );
        assertTrue(ex.getMessage().contains("Evaluation number must be greater than 0"));
    }

    @Test
    void validateIterationNumberNegative() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateExperimentParams("uf1", "nsga-ii",
                        List.of("spacing"), 100, -5)
        );
        assertTrue(ex.getMessage().contains("Iteration number must be greater than 0"));
    }

    @Test
    void validateDatesStartAfterEnd() {
        Timestamp start = Timestamp.valueOf("2024-01-02 00:00:00");
        Timestamp end = Timestamp.valueOf("2024-01-01 00:00:00");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateStatsParams("uf1", "nsga-ii", start, end, new ArrayList<>())
        );
        assertTrue(ex.getMessage().contains("Start date cannot be after end date."));
    }

    @Test
    void validateDatesNullEnd() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateStatsParams("uf1", "gde3",
                        Timestamp.valueOf("2024-01-01 00:00:00"), null, new ArrayList<>())
        );
        assertTrue(ex.getMessage().contains("End date cannot be null."));
    }

    @Test
    void validateStatusUnknown() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                validator.validateListParams(List.of("whatever"), List.of("uf1"),
                        List.of("nsga-ii"), List.of("hypervolume"))
        );
        assertTrue(ex.getMessage().contains("Unknown status specified: whatever"));
    }

    @Test
    void validateStatusOk() {
        assertDoesNotThrow(() ->
                validator.validateListParams(List.of("READY", "COMPLETED"),
                        List.of("uf1"), List.of("nsga-ii"), List.of("spacing"))
        );
    }
}
