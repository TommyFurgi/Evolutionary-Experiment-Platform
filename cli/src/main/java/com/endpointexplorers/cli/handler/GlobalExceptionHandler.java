package com.endpointexplorers.cli.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;


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

    public static void handleResourceAccessError(ResourceAccessException e) {
        System.err.println(setRedColor("Could not connect to server. Please check if it is running on the correct port."));
    }

    private static String setRedColor(String message) {
        return REDCOLOR + message + WHITECOLOR;
    }
}
