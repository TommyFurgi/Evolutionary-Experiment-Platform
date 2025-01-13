package com.example.Endpoint_Explorers.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Data
@NoArgsConstructor
@Getter
@Setter
public class GroupUpdateRequest {
    @NotNull(message = "Experiment IDs cannot be null")
    @NotEmpty(message = "Experiment IDs list must contain at least one ID")
    private List<@Min(value = 1, message = "Experiment ID must be greater than 0") Integer> experimentIds;

    @NotNull(message = "Group name cannot be null")
    @Size(min = 1, message = "Group name must be at least 1 character long")
    private String newGroupName;
}

