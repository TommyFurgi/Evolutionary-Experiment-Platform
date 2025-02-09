package com.example.Endpoint_Explorers.component;

import com.example.Endpoint_Explorers.model.Experiment;
import com.example.Endpoint_Explorers.model.ExperimentDto;
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
