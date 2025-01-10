package com.example.Endpoint_Explorers.model;

import java.util.Arrays;

public enum StatEnum {
    AVG,
    MEDIAN,
    STD_DEV;

    public static StatEnum extractStatsType(String statType) {
        try {
            return StatEnum.valueOf(statType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect statistics type: " + statType
                    + " Choose one of the following: " + Arrays.toString(StatEnum.values()));
        }
    }
}
