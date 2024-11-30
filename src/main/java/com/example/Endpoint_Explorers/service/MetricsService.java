package com.example.Endpoint_Explorers.service;


import com.example.Endpoint_Explorers.repository.MetricsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MetricsRepository repository;

}
