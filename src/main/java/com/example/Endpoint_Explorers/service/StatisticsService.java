package com.example.Endpoint_Explorers.service;

import com.example.Endpoint_Explorers.request.StatsRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

@Service
public class StatisticsService {

    public void getStatsTimeInterval(StatsRequest request) {
        Timestamp startDate = request.getStartDate();
        Timestamp endDate = request.getEndDate();
        System.out.println("Start Date: " + startDate + ", End Date: " + endDate);
    }
}
