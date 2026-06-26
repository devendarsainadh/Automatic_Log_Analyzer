package com.example.ai_log_analyzer.service;

import com.example.ai_log_analyzer.model.AnalysisResult;
import com.example.ai_log_analyzer.model.LogEntry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class AIAnalysisService {

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String groqApiKey;
    private final String groqModel;

    public AIAnalysisService(ObjectMapper objectMapper,
                             @Value("${groq.api-key:}") String groqApiKey,
                             @Value("${groq.model:llama-3.1-8b-instant}") String groqModel) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newHttpClient();
        this.groqApiKey = groqApiKey;
        this.groqModel = groqModel;
    }

    public List<AnalysisResult> analyze(List<LogEntry> logEntries) {
        return logEntries.stream()
                .map(this::analyzeSingleLog)
                .toList();
    }

    private AnalysisResult analyzeSingleLog(LogEntry logEntry) {
        String prompt = """
                You are an experienced Java Production Support Engineer.
                Analyze the following application log.
                Return the response in JSON with the following fields:
                errorType, severity, rootCause, suggestedFix, summary.
                Keep the response concise.
                Log:
                %s
                Return JSON only.
                """.formatted(logEntry.getLevel() + " " + logEntry.getMessage());

        String rawResponse = callGroq(prompt);
        return parseResponse(rawResponse, logEntry);
    }

    private String callGroq(String prompt) {
        if (groqApiKey == null || groqApiKey.isBlank()) {
            throw new IllegalStateException("Groq API key is not configured.");
        }

        try {
            Map<String, Object> requestBody = Map.of(
                    "model", groqModel,
                    "messages", List.of(Map.of(
                            "role", "user",
                            "content", prompt
                    )),
                    "temperature", 0.2
            );

            String body = objectMapper.writeValueAsString(requestBody);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.groq.com/openai/v1/chat/completions"))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + groqApiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("Groq API request failed with status " + response.statusCode() + ": " + response.body());
            }

            JsonNode root = objectMapper.readTree(response.body());
            return root.path("choices").path(0).path("message").path("content").asText();
        } catch (Exception ex) {
            throw new IllegalStateException("Groq API call failed: " + ex.getMessage(), ex);
        }
    }

    private AnalysisResult parseResponse(String rawResponse, LogEntry logEntry) {
        try {
            String json = extractJson(rawResponse);
            AnalysisResult result = objectMapper.readValue(json, AnalysisResult.class);
            result.setOriginalLog(logEntry.getLevel() + " " + logEntry.getMessage());
            return result;
        } catch (JsonProcessingException ex) {
            return AnalysisResult.builder()
                    .errorType("UNKNOWN")
                    .severity("UNKNOWN")
                    .rootCause("Unable to parse AI response")
                    .suggestedFix("Review the log manually")
                    .summary("AI response could not be parsed")
                    .originalLog(logEntry.getLevel() + " " + logEntry.getMessage())
                    .build();
        }
    }

    private String extractJson(String rawResponse) {
        int start = rawResponse.indexOf('{');
        int end = rawResponse.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return rawResponse.substring(start, end + 1);
        }
        return rawResponse;
    }
}
