package com.endpointexplorers.server.component;

import com.endpointexplorers.server.model.Experiment;
import com.endpointexplorers.server.model.ExperimentDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ExperimentDtoMapper {

    public List<ExperimentDto> convertToDtoList(List<Experiment> experiments) {
        return experiments.stream()
                .map(this::createDto)
                .collect(Collectors.toList());
    }

    public ExperimentDto createDto(Experiment experiment) {
        return new ExperimentDto(
                experiment.getId(),
                experiment.getProblemName(),
                experiment.getAlgorithm(),
                experiment.getNumberOfEvaluation(),
                experiment.getStatus(),
                experiment.getDatetime(),
                experiment.getGroupName()
        );
    }
}
