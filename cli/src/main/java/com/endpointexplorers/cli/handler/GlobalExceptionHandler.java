package com.endpointexplorers.cli.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

public class GlobalExceptionHandler {
    public static final String REDCOLOR = "\033[31m";
    public static final String WHITECOLOR = "\033[0m";
    public static void handleHttpClientError(HttpClientErrorException e, String message) {
        String errorMessage = e.getResponseBodyAsString();
        System.err.println(setRedColor(message + errorMessage));
    }

    public static void handleJsonMappingError(JsonMappingException e) {
        System.err.println(setRedColor("JSON mapping error: " + e.getMessage()));
    }

    public static void handleJsonProcessingError(JsonProcessingException e) {
        System.err.println(setRedColor("JSON processing error: " + e.getMessage()));
    }

    private static String setRedColor(String message) {
        return REDCOLOR + message + WHITECOLOR;
    }
}
